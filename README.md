# Miner
Utility to 'mine' data in xdr files and others (`.add`, `.unl`, `.UNL`...).
* Mines files looking for a certain string, tipically an id
* Each line of an xdr file contains several fields separated by a char (default '`|`'), Miner splits each line and compares the string looked for field by field. A line matches if some field matches the string looked for completely. Optionally, the string to match can be a regexp
* Lookup is over the paths given as parameters. If some path is a folder, it is mined recursively
* Several actions can be defined if some file line contains the string looked for:
** Print all lines to sdtout (optionally with the file path appended)
** Print only file names to `sdtout`
** Copy matching files to a given folder, optionally only the matching lines are copied

## To create executable
```bash
sbt assembly
```
Executable will be in `target/scala-2.12/miner`

Once the executable is created, run `miner --help` to see the program arguments.

