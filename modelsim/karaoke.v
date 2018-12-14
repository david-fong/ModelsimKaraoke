`include "definitions.vh"

module karaoke();
    reg  clk, rst;
    wire [`CHAR_H-1:0] sl0, sl1;

    sublistROM #("[36]_Broken_Debugger.txt_sl0") buslistROMx0(clk);
    sublistROM #("[36]_Broken_Debugger.txt_sl1") buslistROMx1(clk);

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