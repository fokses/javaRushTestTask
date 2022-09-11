package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.service.BadRequestException;
import com.game.service.NotFoundException;
import com.game.service.PlayerService;
import com.game.service.PredicateTypes;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import java.util.*;

@RestController
@RequestMapping("/rest/players")
class PlayerController {

    @Autowired
    private PlayerRepository repository;

    @Autowired
    @Resource(name = "playerLogger")
    private Logger logger;

    @GetMapping
    List<Player> getAll(
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String title,
                        @RequestParam(required = false) Race race,
                        @RequestParam(required = false) Profession profession,
                        @RequestParam(required = false) Long after,
                        @RequestParam(required = false) Long before,
                        @RequestParam(required = false) Boolean banned,
                        @RequestParam(required = false) Integer minExperience,
                        @RequestParam(required = false) Integer maxExperience,
                        @RequestParam(required = false) Integer minLevel,
                        @RequestParam(required = false) Integer maxLevel,
                        @RequestParam(required = false, defaultValue = "ID") PlayerOrder order,
                        @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
                        @RequestParam(required = false, defaultValue = "3") Integer pageSize,
                        @RequestParam(required = false) Map<String, String> urlParams
                        ) {

        if (logger.isDebugEnabled()) { logger.debug("getAll: " + urlParams.toString()); }

        Sort sort = Sort.by(order.getFieldName());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Player> specPlayers = getSpecPlayers(name, title, race, profession, after, before,
                banned, minExperience, maxExperience, minLevel, maxLevel, pageable);

        return PlayerService.getPlayers(repository.findAll(specPlayers, pageable));
    }

    @GetMapping("/count")
    Integer getCount(@RequestParam(required = false) String name,
                     @RequestParam(required = false) String title,
                     @RequestParam(required = false) Race race,
                     @RequestParam(required = false) Profession profession,
                     @RequestParam(required = false) Long after,
                     @RequestParam(required = false) Long before,
                     @RequestParam(required = false) Boolean banned,
                     @RequestParam(required = false) Integer minExperience,
                     @RequestParam(required = false) Integer maxExperience,
                     @RequestParam(required = false) Integer minLevel,
                     @RequestParam(required = false) Integer maxLevel,
                     @RequestParam(required = false) Map<String, String> urlParams) {
        if (logger.isDebugEnabled()) { logger.debug("getCount: " + urlParams.toString()); }

        Specification<Player> specPlayers = getSpecPlayers(name, title, race, profession, after, before,
                banned, minExperience, maxExperience, minLevel, maxLevel, Pageable.unpaged());

        return (int) repository.count(specPlayers);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Player createPlayer(@RequestBody Player player) {
        if (!PlayerService.check(player)) {
            String message = String.format("Error validating new player: %s", player);
            logger.error(message);
            throw new BadRequestException(message);
        }
        repository.save(player);

        return player;
    }

    @GetMapping("{id}")
    public Player getPlayer(@PathVariable String id) {
        if (logger.isDebugEnabled()) { logger.debug("getPlayer: " + id); }
        checkStringId(id);
        return getPlayerOrThrowEx(Long.parseLong(id));
    }

    @PostMapping("{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Transactional
    public Player updatePlayer(@PathVariable String id, @RequestBody Player player) {
        if (logger.isDebugEnabled()) { logger.debug(String.format("updatePlayer: %s %s", id, player)); }

        checkStringId(id);
        Player dbPlayer = getPlayerOrThrowEx(Long.parseLong(id));

        if (!PlayerService.check(player, false)) {
            String message = String.format("Error validating new player: %s", player);
            logger.error(message);
            throw new BadRequestException(message);

        }

        PlayerService.updatePlayer(player,dbPlayer);
        repository.save(dbPlayer);

        return dbPlayer;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void deletePlayer(@PathVariable String id) {
        checkStringId(id);

        Long playerId = Long.valueOf(id);

        if (repository.countPlayersById(playerId) == 0)
            throw new NotFoundException("Player not found");

        repository.deleteById(playerId);
    }

    private Specification<Player> getSpecPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, Pageable pageable) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("getPlayer: %s %s %s %s %d %d %s &d &d %d %d", name, title, race, profession,
                    after, before, banned, minExperience, maxExperience, minLevel, maxLevel));
        }

        Specification<Player> spec = getAlwaysTrueSpec();

        if (name != null)
            spec = spec.and(new SpecificationPlayer<String>("name", PredicateTypes.LIKE, name));

        if (title != null)
            spec = spec.and(new SpecificationPlayer<String>("title", PredicateTypes.LIKE, title));

        if (race != null)
            spec = spec.and(new SpecificationPlayer<Race>("race", PredicateTypes.EQUAL, race));

        if (profession != null)
            spec = spec.and(new SpecificationPlayer<Profession>("profession", PredicateTypes.EQUAL, profession));

        if (after != null)
            spec = spec.and(new SpecificationPlayer<Date>("birthday", PredicateTypes.GE, new Date(after)));

        if (before != null)
            spec = spec.and(new SpecificationPlayer<Date>("birthday", PredicateTypes.LE, new Date(before)));

        if (banned != null)
            spec = spec.and(new SpecificationPlayer<Boolean>("banned", PredicateTypes.EQUAL, banned));

        if (minExperience != null)
            spec = spec.and(new SpecificationPlayer<Integer>("experience", PredicateTypes.GE, minExperience));

        if (maxExperience != null)
            spec = spec.and(new SpecificationPlayer<Integer>("experience", PredicateTypes.LE, maxExperience));

        if (minLevel != null)
            spec = spec.and(new SpecificationPlayer<Integer>("level", PredicateTypes.GE, minLevel));

        if (maxLevel != null)
            spec = spec.and(new SpecificationPlayer<Integer>("level", PredicateTypes.LE, maxLevel));

//        Page<Player> page = repository.findAll(spec, pageable);
        return spec;
    }

    private void checkStringId(String idString) {
        double id;

        try {
            id = Double.parseDouble(idString);
        } catch (Exception e) {
            String message = String.format("%s: id must be integer!",idString);
            logger.error(message);
            throw new BadRequestException(message);
        }

        if (id % 1 != 0.0) {
            String message = String.format("%s: id must be integer!",idString);
            logger.error(message);
            throw new BadRequestException(message);
        }

        if (id <= 0) {
            String message = String.format("%s: id must be positive!",idString);
            logger.error(message);
            throw new BadRequestException(message);
        }
    }

    private Player getPlayerOrThrowEx(long id) {
        Optional<Player> p = repository.findById(id);

        if (p.isPresent())
            return p.get();
        else {
            String message = String.format("Player %d not found", id);
            logger.error(message);
            throw new NotFoundException(message);
        }
    }

    private static Specification<Player> getAlwaysTrueSpec() {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.and();
            }
        };
    }

    private static class SpecificationPlayer<T> implements Specification<Player> {
        private final String fieldName;
        private final PredicateTypes type;
        private final T value;

        public SpecificationPlayer(String fieldName, PredicateTypes type, T value) {
            this.fieldName = fieldName;
            this.type = type;
            this.value = value;
        }

        @Override
        public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            switch (type) {
                case LIKE:
                    return criteriaBuilder.like(root.<String>get(fieldName), "%" + value.toString() + "%");
                case GE:
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), (Comparable) value);
                case LE:
                    return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), (Comparable)value);
                case EQUAL:
                    return criteriaBuilder.equal(root.<T>get(fieldName), value);
                default:
                    throw new IllegalArgumentException("Unknown predicate type!");
            }
        }
    }
}


