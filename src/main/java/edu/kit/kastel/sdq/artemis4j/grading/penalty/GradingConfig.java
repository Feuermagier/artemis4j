package edu.kit.kastel.sdq.artemis4j.grading.penalty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.sdq.artemis4j.grading.ProgrammingExercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GradingConfig {
    private static final Logger log = LoggerFactory.getLogger(GradingConfig.class);
    private final String shortName;
    private final List<RatingGroup> ratingGroups;
    private final long validExerciseId;

    private GradingConfig(String shortName, List<RatingGroup> ratingGroups, long validExerciseId) {
        this.shortName = shortName;
        this.ratingGroups = ratingGroups;
        this.validExerciseId = validExerciseId;
    }

    public static GradingConfig readFromString(String configString, ProgrammingExercise exercise) throws InvalidGradingConfigException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            var configDTO = mapper.readValue(configString, GradingConfigDTO.class);

            // no allowed exercises means it is valid for all exercises
            if (!configDTO.allowedExercises().isEmpty() && !configDTO.allowedExercises().contains(exercise.getId())) {
                throw new InvalidGradingConfigException("Grading config is not valid for exercise with id " + exercise.getId());
            }

            var ratingGroups = configDTO.ratingGroups().stream().map(RatingGroup::new).toList();
            var ratingGroupsById = ratingGroups.stream().collect(Collectors.toMap(RatingGroup::getId, Function.identity()));

            for (MistakeType.MistakeTypeDTO dto : configDTO.mistakeTypes()) {
                // TODO add back in maybe?
                /*if (!StringUtil.matchMaybe(exercise.getShortName(), dto.enabledForExercises())) {
                    continue;
                }*/

                MistakeType.createAndAddToGroup(
                    dto,
                    StringUtil.matchMaybe(exercise.getShortName(), dto.enabledPenaltyForExercises()),
                    ratingGroupsById.get(dto.appliesTo())
                );
            }

            var config = new GradingConfig(configDTO.shortName(), ratingGroups, exercise.getId());
            log.info("Parsed grading config for exercise '{}' and found {} mistake types", config.getShortName(), config.getMistakeTypes().size());
            return config;
        } catch (JsonProcessingException e) {
            throw new InvalidGradingConfigException(e);
        }
    }

    public List<MistakeType> getMistakeTypes() {
        return this.streamMistakeTypes().toList();
    }

    public Optional<MistakeType> getMistakeTypeById(String id) {
        return this.streamMistakeTypes()
                .filter(mistakeType -> mistakeType.getId().equals(id))
                .findFirst();
    }

    public String getShortName() {
        return shortName;
    }

    public List< RatingGroup> getRatingGroups() {
        return Collections.unmodifiableList(ratingGroups);
    }

    public boolean isValidForExercise(ProgrammingExercise exercise) {
        return exercise.getId() == validExerciseId;
    }

    @Override
    public String toString() {
        return "GradingConfig[" +
                "shortName=" + shortName + ", " +
                "ratingGroups=" + ratingGroups + ']';
    }

    private Stream<MistakeType> streamMistakeTypes() {
        return ratingGroups.stream()
                .map(RatingGroup::getMistakeTypes)
                .flatMap(List::stream);
    }


    /* package-private */ record GradingConfigDTO(String shortName,
                                                  List<Long> allowedExercises,
                                                  List<RatingGroup.RatingGroupDTO> ratingGroups,
                                                  List<MistakeType.MistakeTypeDTO> mistakeTypes) {
    }
}
