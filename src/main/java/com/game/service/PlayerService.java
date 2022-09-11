package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PlayerService {
    public static List<Player> getPlayers(Iterable<Player> playerIterable) {
        List<Player> players = new ArrayList<>();

        for (Player player : playerIterable) {
            players.add(player);
        }

        return players;
    }

    public static boolean checkCrucialFieldsNotNull(Player player) {
        if (player.getName() == null || player.getTitle() == null || player.getRace() == null
                || player.getProfession() == null || player.getBanned() == null || player.getExperience() == null)
            return false;

        return true;
    }

    public static boolean check(Player player) {
        return check(player, true);
    }

    public static boolean check(Player player, boolean checkNotNull) {

        //if we create user, we must check crucial fields not null
        //if we update, we must not
        if (checkNotNull && !checkCrucialFieldsNotNull(player))
            return false;

        String name = player.getName();
        if (name != null && (name.length() > 12 || name.length() == 0))
            return false;

        String title = player.getTitle();
        if (title != null && title.length() > 30) {
            return false;
        }

        Date birthday = player.getBirthday();
        if (birthday != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(birthday);

            int year = c.get(Calendar.YEAR);
            if (year < 2000 || year > 3000)
                return false;
        }

        Integer experience = player.getExperience();
        if (experience != null && (experience < 0 || experience > 10_000_000))
            return false;

        return true;
    }

    public static void updatePlayer(Player source, Player dest) {
        String name = source.getName();
        if (name != null)
            dest.setName(name);

        String title = source.getTitle();
        if (title != null)
            dest.setTitle(title);

        Race race = source.getRace();
        if (race != null)
            dest.setRace(race);

        Profession profession = source.getProfession();
        if (profession != null)
            dest.setProfession(profession);

        Date birthday = source.getBirthday();
        if (birthday !=null)
            dest.setBirthday(birthday);

        Boolean banned = source.getBanned();
        if (banned != null)
            dest.setBanned(banned);

        Integer experience = source.getExperience();
        if (experience != null)
            dest.setExperience(experience);
    }

}
