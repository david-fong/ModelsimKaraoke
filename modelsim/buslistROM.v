`include "definitions.vh"

// ME, AN INTELLECTUAL:
// *do not modify this file.
module buslistROM(clk, subLine);
    parameter file = "[36] Broken Debugger_sl0.txt";
    input  clk;
    reg    [`CHAR_H-1:0] busList [`LINE_N*`CPSBLN*`ADDR_W-1:0];
    output [`CHAR_H-1:0] subLine;
    reg    [`ADDR_W-1:0] pc;

    initial $readmemb(file, busList);
    initial pc = 0;
    always @(posedge clk) pc = pc + 1'b1;
    assign subLine = busList[pc];
endmodule // buslistROM