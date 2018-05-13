package com.ligresoftware.solete.utils;

public class Utils {
    public static String getDateFormatted(String month, String day) {
        String result = "";

        switch (month) {
            case "1":
                result = "Ene. ";
                break;
            case "2":
                result = "Feb. ";
                break;
            case "3":
                result = "Mar. ";
                break;
            case "4":
                result = "Abr. ";
                break;
            case "5":
                result = "May. ";
                break;
            case "6":
                result = "Jun. ";
                break;
            case "7":
                result = "Jul. ";
                break;
            case "8":
                result = "Ago. ";
                break;
            case "9":
                result = "Sep. ";
                break;
            case "10":
                result = "Oct. ";
                break;
            case "11":
                result = "Nov. ";
                break;
            case "12":
                result = "Dic. ";
                break;
        }

        return result + day;
    }
}
