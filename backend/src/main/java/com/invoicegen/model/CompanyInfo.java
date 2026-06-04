package com.invoicegen.model;

/** Identité de l'émetteur ou du destinataire. */
public class CompanyInfo {
    private String name;
    private String address;
    private String city;
    private String country;
    private String email;
    private String phone;
    private String siret;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }
}
