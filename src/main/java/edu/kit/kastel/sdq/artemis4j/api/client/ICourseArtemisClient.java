/* Licensed under EPL-2.0 2022-2023. */
package edu.kit.kastel.sdq.artemis4j.api.client;

import edu.kit.kastel.sdq.artemis4j.api.ArtemisClientException;
import edu.kit.kastel.sdq.artemis4j.api.artemis.Course;
import edu.kit.kastel.sdq.artemis4j.api.artemis.User;

import java.util.List;

/**
 * REST-Client to execute calls concerning courses.
 */
public interface ICourseArtemisClient {

	/**
	 * Returns all courses for current user. Needs extra rights to be called.
	 *
	 * @return all available courses, containing exercises and available submissions
	 * @throws ArtemisClientException if some errors occur while parsing the result.
	 */
	List<Course> getCourses() throws ArtemisClientException;

	/**
	 * Get teaching assistants for the given course.
	 *
	 * @param course course to load teaching assistants.
	 * @return teaching assistants for the given course.
	 */
	List<User> getTAs(Course course) throws ArtemisClientException;

	/**
	 * Remove a teaching assistant from the given course.
	 */
	void removeTAFromCourse(Course course, User teachingAssistant) throws ArtemisClientException;
}
