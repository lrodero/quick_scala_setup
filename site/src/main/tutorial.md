Cats-effect tutorial
====================
[Cats-effect](https://typelevel.org/cats-effect), the effects library for [Cats](https://typelevel.org/cats), has a
complete documentation explaining the types it brings, full with small examples on how to use them. However, even with
that documentation available, it can be a bit daunting start using the library for the first time.

This tutorial tries to close that gap by means of two examples. The first one shows how to copy the contents from one
file to another. That should help us to flex our muscles. The second one, being still a small example, is fairly more
complex. It shows how to code a TCP server able to attend several clients at the same time, each one being served by its
own Fiber. In several iterations we will create new versions of that server with addded functionality that will require
using more and more concepts of cats-effect.

Setting things up
-----------------
To easy coding this tutorial it is recommended to use `sbt` as the build tool. This is a possible `build.sbt` file for
the project:
```
name := "cats-effect-tutorial"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.0-RC2" withSources() withJavadoc()

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification")
```

Copying contents of a file
--------------------------
Here we will code a function that copies the content from a file to another file. The function takes as parameters the
source and destination files. But this is functional programming! So invoking the function will not copy anything,
instead it returns an `IO` instance that encapsulates all the side-effects involved (opening files, copying content,
etc.), that way _purity_ is kept. Only when that `IO` instance is evaluated will all those side-effects run. In our
implementation the `IO` instance will return the amount of bytes copied upon execution, but this is just a design
decisssion. All this said, the signature of our function looks like this:

```scala
import cats.effect.IO
import java.io.File

def copy(origin: File, destination: File): IO[Long] = ???
```

Nothing scary, uh? Now, let's go step-by-step to implement our function. First thing to do, we need to open two streams
that will read and write file contents. We consider opening an stream to be a side-effect action, so we have to
encapsulate those actions in their own `IO` instances.

```scala
import cats.effect.IO

impor java.io._

val in: IO[InputStream]  = IO{ new BufferedInputStream(new FileInputStream(origin)) }
val out:IO[OutputStream] = IO{ new BufferedOutputStream(new FileOutputStream(destination)) }
```

We want to ensure that once we are done copying both streams are close. For that we will use `bracket`. There are three
stages when using `bracket`: resource adquisition, usage, and release. Thus we define our `copy` function as follows:

```scala
// From now on we assume these three imports to be present in all code snippets
import cats.effect.IO
import cats.implicits._ 
import java.io._ 

def copy(origin: File, destination: File): IO[Long] = {
  val in: IO[InputStream]  = IO{ new BufferedInputStream(new FileInputStream(origin)) }
  val out:IO[OutputStream] = IO{ new BufferedOutputStream(new FileOutputStream(destination)) }

  (in, out)                  // Stage 1: Getting resources 
    .tupled                  // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
    .bracket{
      case (in, out) =>
        transfer(in, out)    // Stage 2: Using resources (for copying data, in this case)
    } {
      case (in, out) =>      // Stage 3: Freeing resources
        (IO{in.close()}, IO{out.close()})
        .tupled              // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
        .handleErrorWith(_ => IO.unit) *> IO.unit
    }
}
```

So far so good, we have our streams ready to go! And in a safe manner too, as the `bracket` construct ensures they are
closed no matter what. Note that any possible error raised when closing is 'swallowed' by the `handleErrorWith` call,
which in the code above basically ignores the error cause. Not ellegant, but enough for this example. Anyway in the real
world there would be little else to do save maybe showing a warning message. Finally we chain the closing action using
`*>` call to return an `IO.unit` after closing (note that `*>` is like using `flatMap` but the second step does not need
input from the first).

By `bracket` contract the action happens in what we have called _Stage 2_, where given the resources we must return an
`IO` instance that perform the task at hand.

For the sake of clarity, the actual construction of that `IO` will be done by a different function, `transfer`. That
`IO` encapsulates a loop that at each iteration will read a _batch_ from the input stream to an array, and then write
the data read to the output stream. Prior to create that function we will create another `transmit` function that simply
moves data from an stream to another using an array as data buffer:

```scala
def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte]): IO[Int] =
  for {
    amount <- IO{ origin.read(buffer, 0, buffer.size) }
    _      <- if(amount > -1) IO { destination.write(buffer, 0, amount) }
              else IO.unit // End of read stream reached (by java.io.InputStream contract), nothing to write
  } yield amount // Returns the actual amount of bytes transmitted
```

Note that both input and output actions are encapsulated in their own `IO` instances. Being `IO` a monad we concatenate
them using a for-comprehension to create another `IO`. Now, with `transmit` we can built the transmission loop in its
own function:

```scala
def transmitLoop(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
  for {
    _      <- IO.cancelBoundary                     // Cancelable at each iteration
    amount <- transmit(origin, destination, buffer) // Make the actual transfer
    total  <- if(amount > -1) transmitLoop(origin, destination, buffer, acc + amount) // Stack safe!
              else IO.pure(acc)                     // Negative 'amount' signals end of input stream
  } yield total
```

There are several things to note in this function. First, the for-comprehension loops as long as the call to `transmit`
does not return a negative value, by means of recursive calls. But `IO` is stack safe, so we are not concerned about
stack overflow issues. At each iteration we increase the counter `acc` with the amount of bytes read at that iteration.
Also, we introduce a call to `IO.cancelBoundary` as the first step of the loop. This is not mandatory for the actual
transference of data we aim for. But it is a good policy, as it marks where the `IO` evaluation will be stopped
(cancelled) if requested. In this case, at each iteration.

So far so good! We are almost there, we only need to allocate the buffer that will be used for moving data around. It
could have been created by `transmitLoop` itself but then we would need to refactor the function to prevent creating a
new array at each iteration. That will be done by our `transfer` function (by convenience we hardcode the buffer size to
10KBs, but that can be easily be made configurable):

```scala
def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
  for {
    buffer <- IO{ new Array[Byte](1024 * 10) } // Allocated only when the IO is evaluated
    acc    <- transmitLoop(origin, destination, buffer, 0L)
  } yield acc
```

And that is it! We are done, now we can create a program that tests this function. We will use `IOApp` for that, as it
allows to maintain purity in our definitions up to the main function. So our final code will look like:

```scala
package tutorial

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._ 
import java.io._ 

object Main extends IOApp {

  def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte]): IO[Int] =
    for {
      amount <- IO{ origin.read(buffer, 0, buffer.size) }
      _      <- if(amount > -1) IO { destination.write(buffer, 0, amount) }
                else IO.unit // End of read stream reached (by java.io.InputStream contract), nothing to write
    } yield amount // Returns the actual amount of bytes transmitted

  def transmitLoop(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
    for {
      _      <- IO.cancelBoundary                     // Cancelable at each iteration
      amount <- transmit(origin, destination, buffer) // Make the actual transfer (our previous function)
      total  <- if(amount > -1) transmitLoop(origin, destination, buffer, acc + amount) // Stack safe!
                else IO.pure(acc)                     // Negative 'amount' signals end of input stream
    } yield total

  def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
    for {
      buffer <- IO{ new Array[Byte](1024 * 10) } // Allocated only when the IO is evaluated
      acc    <- transmitLoop(origin, destination, buffer, 0L)
    } yield acc

  def copy(origin: File, destination: File): IO[Long] = {
    val in: IO[InputStream]  = IO{ new BufferedInputStream(new FileInputStream(origin)) }
    val out:IO[OutputStream] = IO{ new BufferedOutputStream(new FileOutputStream(destination)) }

    (in, out)                  // Stage 1: Getting resources 
      .tupled                  // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
      .bracket{
        case (in, out) =>
          transfer(in, out)    // Stage 2: Using resources (for copying data, in this case)
      } {
        case (in, out) =>      // Stage 3: Freeing resources
          (IO{in.close()}, IO{out.close()})
          .tupled              // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
          .handleErrorWith(_ => IO.unit) *> IO.unit
      }
  }

  // The 'main' function of IOApp //
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _      <- if(args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
                else IO.unit
      orig   <- IO.pure(new File(args(0)))
      dest   <- IO.pure(new File(args(1)))
      copied <- copy(orig, dest)
      _      <- IO{ println(s"$copied bytes copied from ${orig.getPath} to ${dest.getPath}") }
    } yield ExitCode.Success

}
```

You can run this code from `sbt` just by issuing this call:

```
> runMain tutorial.Main
```

Exercises
---------
To finalize we propose you some exercises that will serve you to keep improving your IO-kungfu:
1. If the list of args has less than 2 elements an exception is thrown. This is a bit rough. Create instead an
   `evaluateArgs` function that checks the validity of the arguments given to the main function. This function will
   return an `Either[String, Unit]` instance. In case of error (that is, the list of args has less than two elements),
   the `Left[String]` will contain an error message, `Right[Unit]` will signal that the list of args is fine.
   The function signature will thus be:
   ```scala
   def evaluateArgs(args: List[String]): Either[String, Unit] = ???
   ```
2. Include the `evaluateArgs` function defined above in the `run` function. When it returns `Left[String]` the err
   message will be shown to the user and then the program will finish gracefully. If it returbs `Right[Unit]` the
   execution will continue normally. Feel free to 'break' the for-comprehension in `run` in different parts if that
   helps you.
3. Modify `transmit` so the buffer size is not hardcoded but passed as parameter. That parameter will be passed through 
   `transmitLoop`, `transfer` and `copy` from the main `run` function. Modify the `run` and `evaluateArgs` functions so
   the buffer size can optionally be stated when calling to the program. `evaluateArgs` shall signal error if the third
   arg is present but it is not a valid number. `run` will use the third arg as buffer size if present, if not a default
   hardcode value will be passed to `copy`.
