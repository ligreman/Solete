package com.ligresoftware.solete.utils;

import com.ligresoftware.solete.R;

public class Utils {
    public static String getDateFormatted(String month, String day) {
        String result = "";

        switch (month) {
            case "1":
            case "01":
                result = "Ene. ";
                break;
            case "2":
            case "02":
                result = "Feb. ";
                break;
            case "3":
            case "03":
                result = "Mar. ";
                break;
            case "4":
            case "04":
                result = "Abr. ";
                break;
            case "5":
            case "05":
                result = "May. ";
                break;
            case "6":
            case "06":
                result = "Jun. ";
                break;
            case "7":
            case "07":
                result = "Jul. ";
                break;
            case "8":
            case "08":
                result = "Ago. ";
                break;
            case "9":
            case "09":
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

    public static int getStatusIcon(String status) {
        int icon = R.drawable.ic_info_black_24dp;

        switch (status) {
            // Despejado
            case "11":
                icon = R.drawable.ic_clear;
                break;
            // Despejado noche
            case "11n":
                icon = R.drawable.ic_clear;
                break;
            // Poco nuboso
            case "12":
                icon = R.drawable.ic_clear;
                break;
            // Poco nuboso noche
            case "12n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos
            case "13":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos noche
            case "13n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso
            case "14":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso noche
            case "14n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso
            case "15":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso noche
            case "15n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto
            case "16":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto noche
            case "16n":
                icon = R.drawable.ic_clear;
                break;
            // Nubes altas
            case "17":
                icon = R.drawable.ic_clear;
                break;
            // Nubes altas noche
            case "17n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con lluvia
            case "23":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con lluvia noche
            case "23n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con lluvia
            case "24":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con lluvia noche
            case "24n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con lluvia
            case "25":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con lluvia noche
            case "25n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con lluvia
            case "26":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con lluvia noche
            case "26n":
                icon = R.drawable.ic_clear;
                break;
            // Chubascos
            case "27":
                icon = R.drawable.ic_clear;
                break;
            // Chubascos noche
            case "27n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con nieve
            case "33":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con nieve noche
            case "33n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con nieve
            case "34":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con nieve noche
            case "34n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con nieve
            case "35":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con nieve noche
            case "35n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con nieve
            case "36":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con nieve noche
            case "36n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con lluvia escasa
            case "43":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con lluvia escasa noche
            case "43n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con lluvia escasa
            case "44":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con lluvia escasa noche
            case "44n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con lluvia escasa
            case "45":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con lluvia escasa noche
            case "45n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con lluvia escasa
            case "46":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con lluvia escasa noche
            case "46n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con tormenta
            case "51":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con tormenta noche
            case "51n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con tormenta
            case "52":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con tormenta noche
            case "52n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con tormenta
            case "53":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con tormenta noche
            case "53n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con tormenta
            case "54":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con tormenta noche
            case "54n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con tormenta y lluvia escasa
            case "61":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con tormenta y lluvia escasa noche
            case "61n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con tormenta y lluvia escasa
            case "62":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con tormenta y lluvia escasa noche
            case "62n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con tormenta y lluvia escasa
            case "63":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con tormenta y lluvia escasa noche
            case "63n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con tormenta y lluvia escasa
            case "64":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con tormenta y lluvia escasa noche
            case "64n":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con nieve escasa
            case "71":
                icon = R.drawable.ic_clear;
                break;
            // Intervalos nubosos con nieve escasa noche
            case "71n":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con nieve escasa
            case "72":
                icon = R.drawable.ic_clear;
                break;
            // Nuboso con nieve escasa noche
            case "72n":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con nieve escasa
            case "73":
                icon = R.drawable.ic_clear;
                break;
            // Muy nuboso con nieve escasa noche
            case "73n":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con nieve escasa
            case "74":
                icon = R.drawable.ic_clear;
                break;
            // Cubierto con nieve escasa noche
            case "74n":
                icon = R.drawable.ic_clear;
                break;

            // Granizo
            case "granizo":
                icon = R.drawable.ic_clear;
                break;
            // Granizo noche
            case "granizon":
                icon = R.drawable.ic_clear;
                break;
            // bruma
            case "bruma":
                icon = R.drawable.ic_clear;
                break;
            // bruma noche
            case "bruman":
                icon = R.drawable.ic_clear;
                break;
            // niebla
            case "niebla":
                icon = R.drawable.ic_clear;
                break;
            // niebla noche
            case "nieblan":
                icon = R.drawable.ic_clear;
                break;
            // calima
            case "calima":
                icon = R.drawable.ic_clear;
                break;
            // calima noche
            case "caliman":
                icon = R.drawable.ic_clear;
                break;
        }

        return icon;
    }

    public static int getWindDirectionIcon(String windDirection) {
        int icon = R.drawable.ic_wind_calm;

        switch (windDirection) {
            case "N":
                icon = R.drawable.ic_wind_n;
                break;
            case "NE":
                icon = R.drawable.ic_wind_ne;
                break;
            case "E":
                icon = R.drawable.ic_wind_e;
                break;
            case "SE":
                icon = R.drawable.ic_wind_se;
                break;
            case "S":
                icon = R.drawable.ic_wind_s;
                break;
            case "SO":
                icon = R.drawable.ic_wind_so;
                break;
            case "O":
                icon = R.drawable.ic_wind_o;
                break;
            case "NO":
                icon = R.drawable.ic_wind_no;
                break;
            default:
                icon = R.drawable.ic_wind_calm;
        }

        return icon;
    }
}
