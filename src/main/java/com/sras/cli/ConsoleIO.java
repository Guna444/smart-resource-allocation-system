package com.sras.cli;

import java.util.Scanner;

public final class ConsoleIO {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public void println(String s) {
        System.out.println(s);
    }

    public void printDivider() {
        System.out.println("------------------------------------------------------------");
    }
}