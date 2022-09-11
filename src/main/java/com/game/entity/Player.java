package com.game.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(length = 12)
    private String name;

    @Column(length = 30)
    private String title;

    @Enumerated(EnumType.STRING)
    private Race race;
    @Enumerated(EnumType.STRING)
    private Profession profession;
    private Integer experience;
    private Integer level;
    private Integer untilNextLevel;
    private Date birthday;
    private Boolean banned;

    protected Player() {}

    public Player(String name, String title, Race race, Profession profession, Integer experience, Date birthday, Boolean banned) {
        this.name = name;
        this.title = title;
        this.race = race;
        this.profession = profession;
        this.experience = experience;
        this.birthday = birthday;
        this.banned = banned;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Race getRace() {
        return race;
    }

    public Profession getProfession() {
        return profession;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public Integer getExperience() {
        return experience;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getUntilNextLevel() {
        return untilNextLevel;
    }

    public Boolean getBanned() {
        return banned;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", race=" + race +
                ", profession=" + profession +
                ", experience=" + experience +
                ", level=" + level +
                ", untilNextLevel=" + untilNextLevel +
                ", birthday=" + birthday +
                ", banned=" + banned +
                '}';
    }

    @PrePersist
    @PreUpdate
    public void preSave() {
        if (this.banned == null)
            this.banned = Boolean.FALSE;

        this.level = ((int) Math.sqrt(2500 + 200 * experience) - 50) / 100;
        this.untilNextLevel = 50 * (this.level + 1) * (this.level + 2) - experience;
    }
}
