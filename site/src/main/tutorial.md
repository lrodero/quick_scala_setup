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
stages when using `bracket`: resource adquisition, usage, and release. So let

```scala
import cats.effect.IO
import cats.implicits._ // for tupled

impor java.io._

val in: IO[InputStream]  = IO{ new BufferedInputStream(new FileInputStream(origin)) }
val out:IO[OutputStream] = IO{ new BufferedOutputStream(new FileOutputStream(destination)) }

(in, out)                 // Stage 1: Getting the resources 
  .tupled                 // from (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
  .bracket{
    case (in, out) => ??? // Stage 2: Using the resources
  } {
    case (in, out) =>     // Stage 3: Freeing the resources
      (IO{in.close()}, IO{out.close()})
        .tupled           // transform (IO[Unit], IO[Unit]) into IO[(Unit, Unit)]
        .handleErrorWith(_ => IO.unit) // we 'swallow' errors on clossing... meh
        *> IO.unit        // By bracket contract the release stage must finish with IO[Unit]
  }
```

So far so good, we have our streams ready to go! Copying will be done by a separate function that will read from the
input stream and write to the output stream in _batches_. By 



<!--stackedit_data:
eyJoaXN0b3J5IjpbMTQ2MDExODcyOV19
-->