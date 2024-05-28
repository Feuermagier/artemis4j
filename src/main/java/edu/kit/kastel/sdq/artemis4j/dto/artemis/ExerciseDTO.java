package edu.kit.kastel.sdq.artemis4j.dto.artemis;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kit.kastel.sdq.artemis4j.new_client.ArtemisNetworkException;
import edu.kit.kastel.sdq.artemis4j.new_client.ArtemisClient;
import edu.kit.kastel.sdq.artemis4j.new_client.ArtemisRequest;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record ExerciseDTO(
        @JsonProperty long id,
        @JsonProperty String title,
        @JsonProperty String shortName,
        @JsonProperty String testRepositoryUri,
        @JsonProperty Boolean secondCorrectionEnabled,
        @JsonProperty String exerciseType,
        @JsonProperty String assessmentType,
        @JsonProperty double maxPoints,
        @JsonProperty ZonedDateTime dueDate,
        @JsonProperty ZonedDateTime startDate
) {

    public static List<ExerciseDTO> fetchAll(ArtemisClient client, int courseId) throws ArtemisNetworkException {
        var exercises = ArtemisRequest.get()
                .path(List.of("courses", courseId, "with-exercises"))
                .executeAndDecode(client, ExerciseWrapperDTO.class);
        // Remove all non-programming exercises
        return Arrays.stream(exercises.exercises()).filter(e -> e.exerciseType().equals("PROGRAMMING")).toList();
    }

    private record ExerciseWrapperDTO(ExerciseDTO[] exercises) {
    }
}
