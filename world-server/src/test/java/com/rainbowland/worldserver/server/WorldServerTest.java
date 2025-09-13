package com.rainbowland.worldserver.server;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

class WorldServerTest {

    @Test
    void proto() throws IOException {

        Socket socket = new Socket("127.0.0.1", 8085);
        InputStream inputStream = socket.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

        System.out.println(in.readLine());
        out.print("WORLD OF WARCRAFT CONNECTION - CLIENT TO SERVER - V2\n");
        out.flush();

        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
        System.out.println(in.read());
    }


    public static void main(String[] args) {

        System.out.println(Duration.between(LocalDate.of(2021,4,11),LocalDate.of(2021,3,15)).toMillis());
    }

}