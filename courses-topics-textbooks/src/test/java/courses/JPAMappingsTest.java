package courses;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
public class JPAMappingsTest {
	
	@Resource
	private TestEntityManager entityManager;
	
	@Resource
	private TopicRepository topicRepo;
	
	@Resource
	private CourseRepository courseRepo;
	
	@Resource
	private TextbookRepository textbookRepo;
	
	
	
	@Test
	public void shouldSaveAndLoadTopic() {
		Topic topic = topicRepo.save(new Topic("topic"));
		Long topicId = topic.getId();
		
		entityManager.flush(); //forces jpa to hit database
		entityManager.clear();
		
		Optional<Topic> result = topicRepo.findById(topicId);
		topic = result.get();
		assertThat(topic.getName(), is("topic"));
	}
	
	@Test
	public void shouldGenerateTopicId() {
		Topic topic = topicRepo.save(new Topic("topic"));
		Long topicId = topic.getId();
		
		entityManager.flush();
		entityManager.clear();
		
		assertThat(topicId, is(greaterThan(0L)));
	}

	@Test
	public void shouldSaveAndLoadCourses() {
		Course course = new Course("course name", "description");
		course = courseRepo.save(course);
		long courseId = course.getId();
		
		entityManager.flush();
		entityManager.clear();
		
		Optional<Course> result = courseRepo.findById(courseId);
		course = result.get();
		assertThat(course.getName(), is("course name"));
	}
	
	@Test
	public void shouldEstablishCourseToTopicRelationships() {
		//topic is not the owner so we create these first
		Topic java = topicRepo.save(new Topic("java"));
		Topic ruby = topicRepo.save(new Topic("ruby"));
		
		Course course = new Course("00 Languages", "description", java, ruby);
		course = courseRepo.save(course);
		long courseId = course.getId();
		
		Optional<Course> result = courseRepo.findById(courseId);
		course = result.get();
		
		assertThat(course.getTopics(), containsInAnyOrder(java, ruby));
		
		
	}
	
	@Test
	public void shouldFindCoursesForTopic() {
		Topic java = topicRepo.save(new Topic("java"));
		
		Course ooLanguages = courseRepo.save(new Course("00 Languages", "description", java));
		Course advancedJava = courseRepo.save(new Course("Adv Java", "description", java));
		
		entityManager.flush();
		entityManager.clear();
		
		Collection<Course> coursesForTopic = courseRepo.findByTopicsContains(java);
		
		assertThat(coursesForTopic, containsInAnyOrder(ooLanguages, advancedJava));
	}
	@Test
	public void shouldFindCoursesForTopicId() {
		Topic ruby = topicRepo.save(new Topic("ruby"));
		long topicId = ruby.getId();
		
		Course ooLanguages = courseRepo.save(new Course("00 Languages", "description", ruby));
		Course advancedRuby = courseRepo.save(new Course("Adv Ruby", "description", ruby));
		
		entityManager.flush();
		entityManager.clear();
		
		Collection<Course> coursesForTopic = courseRepo.findByTopicsId(topicId);
		
		assertThat(coursesForTopic, containsInAnyOrder(ooLanguages, advancedRuby));
	}
	
	@Test
	public void shouldSaveTextBookToCourseRelationship() {
		Course course = new Course("name", "description");
		courseRepo.save(course);
		long courseId = course.getId();
		
		Textbook book = new Textbook("title", course);
		textbookRepo.save(book);
		
		Textbook book2 = new Textbook("title", course);
		textbookRepo.save(book2);
		
		entityManager.flush();
		entityManager.clear();
		
		Optional<Course> result = courseRepo.findById(courseId);
		course= result.get();
		assertThat(course.getTextbooks(), containsInAnyOrder(book, book2));
	}
	
	@Test
	public void shouldSortCourses() {
		Course ooLanguages = new Course("OO Languages", "description");
		ooLanguages = courseRepo.save(ooLanguages);
		
		Course scriptingLanguages = new Course("Scripting Languages", "description");
		scriptingLanguages = courseRepo.save(scriptingLanguages);
		
		entityManager.flush();
		entityManager.clear();
		
		Collection<Course> sortedCourses = courseRepo.findAllByOrderByNameAsc();
		assertThat(sortedCourses, contains(scriptingLanguages, ooLanguages));
	}
}
