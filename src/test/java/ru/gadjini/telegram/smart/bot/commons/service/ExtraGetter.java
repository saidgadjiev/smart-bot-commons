package ru.gadjini.telegram.smart.bot.commons.service;

public class ExtraGetter {

    private String name;

    private String fName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public boolean isAdmin() {
        return name.equals("test");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtraGetter that = (ExtraGetter) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return fName != null ? fName.equals(that.fName) : that.fName == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fName != null ? fName.hashCode() : 0);
        return result;
    }
}
