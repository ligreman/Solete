package com.ligresoftware.solete;

public class WeatherListItem {
    private String hora;
    private String fecha;
    private String temperatura;
    private String estado;
    private String precipitacion;
    private String nieve;
    private String viento_velocidad;
    private String viento_direccion;
    private boolean isAhora;
    private boolean isHourly;
    private boolean isDaily;

    /**
     * Constructor
     */
    public WeatherListItem() {
    }

    /**
     * Constructor
     */
    public WeatherListItem(boolean isAhora) {
        this.isAhora = isAhora;
    }

    /**
     * Constructor
     */
    public WeatherListItem(String hora, String fecha, String estado, boolean isAhora, boolean isHourly, boolean isDaily) {
        this.hora = hora;
        this.fecha = fecha;
        this.estado = estado;
        this.isAhora = isAhora;
        this.isHourly = isHourly;
        this.isDaily = isDaily;
    }
    /* GETTERS Y SETTERS */

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrecipitacion() {
        return precipitacion;
    }

    public void setPrecipitacion(String precipitacion) {
        this.precipitacion = precipitacion;
    }

    public String getNieve() {
        return nieve;
    }

    public void setNieve(String nieve) {
        this.nieve = nieve;
    }

    public String getVientoVelocidad() {
        return viento_velocidad;
    }

    public void setVientoVelocidad(String viento_velocidad) {
        this.viento_velocidad = viento_velocidad;
    }

    public String getVientoDireccion() {
        return viento_direccion;
    }

    public void setVientoDireccion(String viento_direccion) {
        this.viento_direccion = viento_direccion;
    }

    public boolean isAhora() {
        return isAhora;
    }

    public void setAhora(boolean ahora) {
        isAhora = ahora;
    }

    public boolean isHourly() {
        return isHourly;
    }

    public void setHourly(boolean hourly) {
        isHourly = hourly;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean daily) {
        isDaily = daily;
    }
}
