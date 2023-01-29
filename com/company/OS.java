package com.company;

import java.io.*;
import java.util.*;
import java.lang.*;

public class OS {

    private static char[][] M = new char[300][4];
    private char[] buffer = new char[40];
    private static Integer[] flag = new Integer[30];
    private static String line;

    // registers
    private static char[] IR = new char[4];
    private static char[] R = new char[4];
    private static int C = 0;
    private static int IC = 0;
    private static int PTR = 0;
    private static int VA = 0;
    private static int RA = 0;
    private static int kio = -1;

    // interupts
    private static int SI = 0;
    private static int PI = 0;
    private static int TI = 0;

    // interupt counters
    private static int TTC = 0;
    private static int LLC = 0;

    private static boolean endProgram = false;

    private String inputFile;
    private String outputFile;
    private FileReader input;
    private BufferedReader fread;
    private FileWriter output;
    private BufferedWriter fwrite;

    PCB pcb = new PCB();
    Random rd = new Random();

    public OS(String ifile, String ofile) {
        this.inputFile = ifile;
        this.outputFile = ofile;

        try {
            this.input = new FileReader(inputFile);
            this.fread = new BufferedReader(input);
            this.output = new FileWriter(outputFile);
            this.fwrite = new BufferedWriter(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void dispMemo() {
        for (int i = 0; i < 300; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.print(M[i][j]);
            }
            System.out.print("\n");
        }
    }

    public void allocate() {
        int pos, check = 0, len, level = 0;
        char[] str = new char[2];

        while (check != 1) {
            kio++; // 0
            pos = Math.abs((rd.nextInt() % 29) * 10); // 140
            while (flag[pos / 10] != 0) {
                pos = Math.abs((rd.nextInt() % 29) * 10);
            }
            flag[pos / 10] = 1; // 14 = 1
            str = Integer.toString(pos).toCharArray(); // 140
            if (pos / 100 == 0) {
                M[PTR + kio][2] = '0';
                M[PTR + kio][3] = str[0];
            } else {
                M[PTR + kio][2] = str[0]; // 240 -> **1*
                M[PTR + kio][3] = str[1]; // 240 -> **14
            }
            try {
                line = fread.readLine(); // GD20PD20H
                buffer = line.toCharArray();
                level++; // 1
                int k = 0;
                for (int i = 0; i < line.length() / 4; i++) { // 3 times
                    for (int j = 0; j < 4; j++) { // 4 times
                        System.out.println(buffer[k]);
                        M[pos + i][j] = buffer[k]; // 140 -> GD20 // 141 -> PD20 // 142 -> H
                        k++; // 8
                        if (buffer[k] == 'H') {
                            check = 1;
                            M[pos + (i + 1)][0] = 'H'; // 143 -> H
                            M[pos + (i + 1)][1] = '0'; // 143 -> H0
                            M[pos + (i + 1)][2] = '0'; // 143 -> H00
                            M[pos + (i + 1)][3] = '0'; // 143 -> H000
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void MOS() {
        try {

            if (PI == 1) {
                System.out.println("4: Opcode Error. Program terminated abnormally.");
                fwrite.write("4: Opcode Error. Program terminated abnormally.");
                endProgram();
            } else if (PI == 2) {
                System.out.println("5: Oprand Error. Program terminated abnormally.");
                fwrite.write("5: Oprand Error. Program terminated abnormally.");
                endProgram();
            }

            if (SI == 3) {
                endProgram();
            } else if (SI == 1) {
                if (TI == 0) {
                    read();
                } else if (TI == 2) {
                    System.out.println("3: Time limit exceeded. Program terminated abnormally.");
                    fwrite.write("3: Time limit exceeded. Program terminated abnormally.");
                    endProgram();
                }
            } else if (SI == 2) {
                if (TI == 0) {
                    write();
                } else if (TI == 2) {
                    write();
                    System.out.println("3: Time limit exceeded. Program terminated abnormally.");
                    fwrite.write("3: Time limit exceeded. Program terminated abnormally.");
                    endProgram();
                } else if (TI == 1) {
                    write();
                    System.out.println("2: Line limit exceeded. Program terminated abnormally.");
                    fwrite.write("2: Line limit exceeded. Program terminated abnormally.");
                    endProgram();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endProgram() {
        try {
            fwrite.write("SI: " + SI + " PI: " + PI + " TI: " + TI + "TTC: " + TTC + " LLC: " + LLC);
            System.out.println("SI: " + SI + " PI: " + PI + " TI: " + TI + "TTC: " + TTC + " LLC: " + LLC);
            fwrite.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void read() {
        int no;
        try {
            line = fread.readLine();
            buffer = line.toCharArray();
            // convert M[RA][2] and M[RA][3] to integer
            no = Integer.parseInt(String.valueOf(M[RA][2]) + String.valueOf(M[RA][3]));
            no = no * 10; // 20
            int k = 0;

            for (int i = 0; k < line.length(); i++) { // i = 1
                for (int j = 0; j < 4 && k <= line.length(); j++) { // j = 1
                    M[no + i][j] = buffer[k]; // m[20][0] = H, M[20][1] = E, M[20][2] = L, M[20][3] = L, M[21][0] = O
                    k++; // 5
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        dispMemo();
    }

    public void write() {
        int no;
        try {
            no = Integer.parseInt(String.valueOf(M[RA][2]) + String.valueOf(M[RA][3]));
            no = no * 10; // 20
            int k = 0;
            while (true) {
                for (int i = 0; i < 4; i++) {
                    if (M[no][i] == '\0') {
                        break;
                    }
                    buffer[k] = M[no][i];
                    k++;
                }
                if (M[no][0] == '\0') {
                    break;
                }
                no++;
            }

            fwrite.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMap() {
        int add, pos;
        char[] str = new char[2];
        RA = PTR + (VA / 10); // 240

        if (M[RA][3] == '*') {
            System.out.println("Page fault occur");
            pos = Math.abs((rd.nextInt() % 29) * 10); // 210
            while (flag[pos / 10] != 0) {
                pos = Math.abs((rd.nextInt() % 29) * 10);
            }
            flag[pos / 10] = 1; // 21 = 1

            str = Integer.toString(pos).toCharArray(); // 210
            if (pos / 100 == 0) {
                M[RA][2] = '0';
                M[RA][3] = str[0];
            } else {
                M[RA][2] = str[0]; // 242 -> **2*
                M[RA][3] = str[1]; // 242 -> **21
            }

            PI = 3;

        }

        if (RA > PTR + 10) {
            // System.out.println("Segmentation fault occur");
            PI = 2;
            MOS();
        }
    }

    public void examine() {
        char ch = IR[0];
        PI = 0;

        switch (ch) {
            case 'G':
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                if (IR[1] != 'D') {
                    PI = 1;
                    MOS();
                } else {
                    TTC = TTC + 2;
                    if (TTC <= pcb.ttl) {
                        SI = 1;
                        MOS();
                    } else {
                        TI = 2;
                        MOS();
                    }
                }
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                break;
            case 'P':
                SI = 2;
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                if (IR[1] != 'D') {
                    PI = 1;
                    MOS();
                } else {
                    LLC = LLC + 1;
                    TTC = TTC + 1;
                    if (LLC < pcb.tll) {
                        TI = 0;
                        MOS();
                    }
                    if (TTC > pcb.ttl) {
                        TI = 1;
                        MOS();
                    } else {
                        SI = 2;
                        MOS();
                    }
                }
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                break;
            case 'L':
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                if (IR[1] != 'R') {
                    PI = 1;
                    MOS();
                } else {
                    TTC++;
                    if (TTC <= pcb.ttl) {
                        for (int i = 0; i < 4; i++)
                            R[i] = M[RA][i];
                    } else {
                        TI = 2;
                        MOS();
                    }
                }
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                break;
            case 'S':
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                if (IR[1] != 'R') {
                    PI = 1;
                    MOS();
                } else {
                    TTC++;
                    if (TTC <= pcb.ttl) {
                        for (int i = 0; i < 4; i++)
                            M[RA][i] = R[i];
                    } else {
                        TI = 2;
                        MOS();
                    }
                }
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                break;
            case 'C':
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                if (IR[1] != 'R') {
                    PI = 1;
                    MOS();
                } else {
                    TTC++;
                    if (TTC <= pcb.ttl) {
                        int res = 0;
                        for (int i = 0; i < 4; i++)
                            if (M[RA][i] != R[i])
                                res = 1;
                        C = res;
                        res = 0;
                    } else {
                        TI = 2;
                        MOS();
                    }
                }
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                break;
            case 'B':
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                if (IR[1] != 'T') {
                    PI = 1;
                    MOS();
                } else {
                    TTC++;
                    if (TTC <= pcb.ttl) {
                        if (C == 1)
                            IC = Integer.parseInt(String.valueOf(IR[2]) + String.valueOf(IR[3]));
                        C = 0;
                    } else {
                        TI = 2;
                        MOS();
                    }
                }
                System.out.println("SI:" + SI + " TI:" + TI + " PI:" + PI + "TTC: " + TTC + " LLC: " + LLC);
                break;
            case 'H':
                SI = 3;
                MOS();
                break;
            default:
                PI = 1;
                MOS();
                break;
        }
    }

    public void executeProgram() {
        int no;
        char[] a = new char[3];
        for (int i = 0; i <= kio; i++) {
            a[0] = M[PTR + i][2]; // 240 -> **14 = 1
            a[1] = M[PTR + i][3]; // 240 -> **14 = 4
            a[2] = '\0';

            no = Integer.parseInt(String.valueOf(a).trim()); // 14
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 4; k++) {
                    IR[k] = M[no * 10 + j][k];
                } // IR = ####
                if (IR[0] != '\0') {
                    System.out.println("IR: " + String.valueOf(IR).trim());

                    VA = Integer.parseInt(String.valueOf(IR).substring(2, 4)); // 00
                    System.out.println("VA: " + VA);
                    addMap();
                    examine();
                }
            }
        }
    }

    public void startExecution() {
        IC = 0;
        executeProgram();
    }

    public void initialize() {
        int i, j;
        PTR = Math.abs((rd.nextInt() % 29) * 10); // 240
        for (i = 0; i < 30; i++) {
            flag[i] = 0;
        }
        System.out.println("PTR: " + PTR); // 24
        flag[PTR / 10] = 1; // 24 = 1
        for (i = 0; i < 300; i++) {
            for (j = 0; j < 4; j++) {
                M[i][j] = '\0';
            }
        }
        for (i = PTR; i < PTR + 10; i++) {
            for (j = 0; j < 4; j++) {
                M[i][j] = '*';
            }
        }
        for (i = 0; i < 4; i++) {
            IR[i] = '\0';
            R[i] = '\0';
        }
        C = IC = VA = RA = SI = PI = TI = TTC = LLC = 0;
    }

    public void load() {
        try {
            while ((line = fread.readLine()) != null) {
                System.out.println(line);
                if (line.contains("$AMJ")) {
                    System.out.println("Found $AMJ");
                    pcb.jobid = line.substring(4, 8);
                    pcb.ttl = Integer.parseInt(line.substring(8, 12));
                    pcb.tll = Integer.parseInt(line.substring(12, 16));

                    initialize();
                    allocate();
                } else if (line.contains("$DTA")) {
                    startExecution();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
