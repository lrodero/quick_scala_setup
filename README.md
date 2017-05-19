# Miner
Utility to 'mine' data in xdr files and others (`.add`, `.unl`, `.UNL`...).
* Mines files looking for a certain string, tipically an id
* Each line of an xdr file contains several fields separated by a char (default '`|`'), Miner splits each line and compares the string looked for field by field. A line matches if some field matches the string looked for completely. Optionally, the string to match can be a regexp
* Lookup is over the paths given as parameters. If some path is a folder, it is mined recursively
* Several actions can be defined if some file line contains the string looked for:
** Print all lines to sdtout (optionally with the file path appended)
** Print only file names to `sdtout`
** Copy matching files to a given folder, optionally only the matching lines are copied

###Examples of use:

```bash
# mine for id 1234567890, recursively in path/to/folder and also in all *add files in this folder
mine --for 123456789 --in path/to/folder *.add
```

###Usage
```
mine [opts] --for [string or regexp to mine] 

Options:
-h --help This message
-s --sep [char] Fields separator, defaults to '|'
-n --norec Turn off recursion when exploring folders
-r --regexp String to lookfor is a regexp
-l --lines Print matching lines to stdout (default if no opt is given)
-L --lines-with-filename Print matching lines with filename appended as a new field at the end
-c --copy [folder] Copy matching files to folder
-C --copy-filter [folder] Copy matching files, but filter out lines that do not match
-i --in [folders and files] Folders and files to mine. Defaults to '.'
-f --for [string] Mandatory, string or regexp to mine
```

