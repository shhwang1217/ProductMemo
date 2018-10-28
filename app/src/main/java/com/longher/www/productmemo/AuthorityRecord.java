package com.longher.www.productmemo;

public class AuthorityRecord {

    private int id;  // 使用者 ID
    private String nickname; // 使用者暱稱
    private String email; // 使用者 E-Mail, 即為帳號
    private String datetime; // 建立帳號的時間, 以 ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
    private String pwdhash; // 密碼需要再 Hash 過再比較

    public AuthorityRecord( String nickname, String email, String datetime, String pwdhash ) {
        this(0, nickname, email, datetime, pwdhash );
    }

    public AuthorityRecord(int id, String nickname, String email, String datetime, String pwdhash ) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.datetime = datetime;
        this.pwdhash = pwdhash;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getPwdHash() {
        return pwdhash;
    }

    public void setPwdHash(String pwdhash) {
        this.pwdhash = pwdhash;
    }
}
