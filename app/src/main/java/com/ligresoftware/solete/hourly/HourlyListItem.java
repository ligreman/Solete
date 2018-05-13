package com.ligresoftware.solete.hourly;

public class HourlyListItem {
    private String hora;
    private String fecha;
    private String temperatura;
    private String estado;
    private String precipitacion;
    private String nieve;
    private String viento_velocidad;
    private String viento_direccion;
    private boolean isAhora;

    /**
     * Constructor
     */
    public HourlyListItem() {
    }

    /**
     * Constructor
     */
    public HourlyListItem(String estado) {
        this.estado = estado;
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
}
