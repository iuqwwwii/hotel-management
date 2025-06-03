package com.hotel.server.model;

public class Service {
    Integer partner_id;
    String name;
    String contactInfo;

    Service(Integer partner_id, String name, String contactInfo) {
        this.partner_id = partner_id;
        this.name = name;
        this.contactInfo = contactInfo;
    }
    public Integer getPartner_id() {return partner_id;}
    public String getName() {return name;}
    public String getContactInfo() {return contactInfo;}

    public void setPartner_id(Integer partner_id) {this.partner_id = partner_id;}
    public void setName(String name) {this.name = name;}
    public void setContactInfo(String contactInfo) {this.contactInfo = contactInfo;}
}

