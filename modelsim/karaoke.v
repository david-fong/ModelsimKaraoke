`include "definitions.vh"

module karaoke();
    reg  clk, rst;
    wire [`CHAR_H-1:0] sl0, sl1, sl2;

    buslistROM #("[36]_Broken_Debugger_3sublines_sl0.txt") blROMx0(clk, sl0);
    buslistROM #("[36]_Broken_Debugger_3sublines_sl1.txt") blROMx1(clk, sl1);
    buslistROM #("[36]_Broken_Debugger_3sublines_sl2.txt") blROMx2(clk, sl2);

    initial forever begin
        repeat (`CPSBLN*`CHAR_W-1) begin
            clk = 1; #5;
            clk = 0; #5;
        end
        $stop;
        clk = 1; #5;
        clk = 0; #5;
    end

    initial begin
        #31680;
        $stop;
    end
endmodule