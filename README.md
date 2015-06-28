# csvfx
csvfx is a tool to change the following parameters of a CSV file:
- separator character
- quote character
- escape character
- file encoding

# Screenshot
<img src="http://i.imgur.com/JNGhi7N.png" title="screenshot" />

# Modes
csvfx provide two operation modes
## Stream copy
The input file is converted by streaming the data to the output file. This mode is perfect when converting a large file. The table will show a limited number of rows to preview the result.
## Editable copy
The output file is written from the table in the tool. This provides the ability to edit the file contents before it gets written to the output file. Keep in mind that the whole file gets loaded into memory so you might end with a OutOfMemoryException on large files.
