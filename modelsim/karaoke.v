`include "definitions.vh"

module karaoke();
    reg  clk, rst;
    wire [`CHAR_H-1:0] sl0, sl1, sl2;

    buslistROM #("[36]_Broken_Debugger_3sublines.txt_sl0") buslistROMx0(clk);
    buslistROM #("[36]_Broken_Debugger_3sublines.txt_sl1") buslistROMx1(clk);
    buslistROM #("[36]_Broken_Debugger_3sublines.txt_sl2") buslistROMx2(clk);

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