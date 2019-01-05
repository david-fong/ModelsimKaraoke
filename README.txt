EXAMPLE: Run Converter.main() with no arguments.
DEMO:    https://www.youtube.com/watch?v=jKpDQr-2eWM

I. USING THIS PROGRAM:
    1. Create a formatted text file (See II).

    2. Run the main program in src/Converter
       with args[0] = <your lyrics filename>
       (include the .txt extension), args[1] =
       desired character height in pixels,
       and args[2] = desired character width in
       pixels. A corresponding font file in the
       fonts directory must exist. A 9x6 font is
        provided (See III).

    3. Create a Modelsim project in the modelsim
       directory and include the following files:
       > buslistROM.v
       > karaoke.v

    4. Open Modelsim and start a simulation of "karaoke".
       Add each top-level signal named "sl<#>" to
       the waveform viewer, expand them, and format
       each bit signal as a literal value. (NOTE:
       Modelsim is not consistent with the order of
       the expanded bits (they might be upside-down).

    5. Repeatedly continue the run to display the
       next line of lyrics. (type "run -continue"
       or click the continue run button).


II. MAKING YOUR OWN FORMATTED LYRIC FILES:
    The first line of the file should contain
    <sl>: the number of horizontal lines of text to display
    in the waveform viewer. The second line should contain
    <cpsl>, the number of characters to fit to the screen
    of the waveform viewer.

    Following these should be lines of lyrics. Each line
    should contain <sl> sub-lines separated by any amount of
    trailing whitespace followed "//". Each sub-line should
    contain no more than <cpsl> characters (including word-
    separating whitespaces).

    Blank lines and those starting with "//" or
    ">>>" are permitted and will be ignored.

    Avoid using whitespace in your filename.
    Save this text file to the modelsim directory.


III. MAKING YOUR OWN FONTS:
    The "font" folder contains files of character mappings.
    Each file should be named "charBinaries_<h>x<w>"
        where <h> is char-height-in-pixels, and
        where <w> is char-width-in-pixels.

    Each line should start with a single character, followed by
        <w> binary number strings of length <h>. Each binary number
        represents a vertical slice of pixels of a character
        (in order of left to right). Each slice's most significant
        bit is the topmost pixel of that slice of the character.
        1 represents a pixel IN a character and 0 represents a pixel
        NOT IN a character.

        The last slice should be all zeros (to separate characters).
    (Note: comment lines following "//..." are permitted and will
        be ignored)
