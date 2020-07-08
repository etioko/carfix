package com.final_project.carfix.ExternalServices;

public class PostRequest {

    private String start, extra, care_category, vendor;
    private int ticket_id, earlier_entries;
    private Integer id;


    public PostRequest(int ticket_id, String start, String extra, String care_category, String vendor, int earlier_entries) {
        this.ticket_id = ticket_id;
        this.start = start;
        this.extra = extra;
        this.care_category = care_category;
        this.vendor = vendor;
        this.earlier_entries = earlier_entries;
    }

    public int getTicket_id() {
        return ticket_id;
    }

    public String getStart() {
        return start;
    }

    public String getExtra() {
        return extra;
    }

    public int getId() {
        return id;
    }

    public String getCare_category() {
        return care_category;
    }

    public String getVendor() {
        return vendor;
    }

    public int getEarlier_entries() {
        return earlier_entries;
    }
}

