package org.example.kidsmathapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.entity.Achievement;
import org.example.kidsmathapp.entity.AvatarItem;
import org.example.kidsmathapp.entity.Game;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.entity.Topic;
import org.example.kidsmathapp.entity.enums.Difficulty;
import org.example.kidsmathapp.entity.enums.GameType;
import org.example.kidsmathapp.entity.enums.GradeLevel;
import org.example.kidsmathapp.entity.enums.QuestionType;
import org.example.kidsmathapp.repository.AchievementRepository;
import org.example.kidsmathapp.repository.AvatarItemRepository;
import org.example.kidsmathapp.repository.GameRepository;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.QuestionRepository;
import org.example.kidsmathapp.repository.TopicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final AchievementRepository achievementRepository;
    private final QuestionRepository questionRepository;
    private final GameRepository gameRepository;
    private final AvatarItemRepository avatarItemRepository;

    private boolean gradeSeeded(GradeLevel grade) {
        return topicRepository.existsByGradeLevel(grade);
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (topicRepository.count() == 0) {
            log.info("Starting data seeding...");
            List<Topic> topics = seedTopicsAndLessons();
            seedAchievements();
            seedGames(topics);
            log.info("Data seeding completed successfully!");
        } else {
            log.info("Database already contains data, skipping kindergarten seeding.");
            // Still seed games if they don't exist
            if (gameRepository.count() == 0) {
                log.info("Seeding games...");
                List<Topic> topics = topicRepository.findAll();
                seedGames(topics);
                log.info("Game seeding completed!");
            }
        }

        // Seed grades 1-3 if not already seeded (per-grade check)
        if (!gradeSeeded(GradeLevel.GRADE_1)) {
            log.info("Seeding Grade 1 content...");
            seedGrade1();
            log.info("Grade 1 seeding completed!");
        }
        if (!gradeSeeded(GradeLevel.GRADE_2)) {
            log.info("Seeding Grade 2 content...");
            seedGrade2();
            log.info("Grade 2 seeding completed!");
        }
        if (!gradeSeeded(GradeLevel.GRADE_3)) {
            log.info("Seeding Grade 3 content...");
            seedGrade3();
            log.info("Grade 3 seeding completed!");
        }

        // Always run question text fixes (idempotent)
        fixNumberRecognitionQuestions();
        fixGameQuestions();
        // Always seed avatar items (idempotent via existsByName check)
        seedAvatarItems();
    }

    @Transactional
    void fixNumberRecognitionQuestions() {
        int fixed = 0;
        // Replace "Which number is this? N" with "Tap the number N!" — answer was visible in question text
        fixed += questionRepository.updateQuestionText("Which number is this? 3", "Tap the number three!");
        fixed += questionRepository.updateQuestionText("Tap the number five", "Tap the number five!");
        fixed += questionRepository.updateQuestionText("Which is the number 2?", "Tap the number two!");
        fixed += questionRepository.updateQuestionText("Which number is this? 8", "Tap the number eight!");
        fixed += questionRepository.updateQuestionText("Tap the number seven", "Tap the number seven!");
        fixed += questionRepository.updateQuestionText("Which is the number 10?", "Tap the number ten!");
        if (fixed > 0) {
            log.info("Fixed {} number recognition question(s)", fixed);
        }
    }

    @Transactional
    void fixGameQuestions() {
        int fixed = 0;
        // Fix Shape Safari: replace conceptual questions with find-shape questions
        fixed += questionRepository.updateQuestionText(
                "What shape has 3 sides?", "Find the triangle!");
        fixed += questionRepository.updateQuestionText(
                "How many corners does a square have?", "Find the diamond!");
        fixed += questionRepository.updateQuestionText(
                "Which shape is round?", "Find the circle! Round round round");
        fixed += questionRepository.updateQuestionText(
                "How many sides does a triangle have?", "Find the triangle! Three sides");

        // Fix Pattern Parade: strip prose prefix, use comma-separated format
        fixed += questionRepository.updateQuestionText(
                "What comes next? 🔴🔵🔴🔵🔴?", "🔴, 🔵, 🔴, 🔵, 🔴, ?");
        fixed += questionRepository.updateQuestionText(
                "Complete: ⭐⭐🌙⭐⭐🌙⭐⭐?", "⭐, 🌙, ⭐, 🌙, ⭐, ?");
        fixed += questionRepository.updateQuestionText(
                "What's missing? 1, 2, 3, ?, 5", "1, 2, 3, ?, 5");
        fixed += questionRepository.updateQuestionText(
                "What comes next? 🔺⭕🔺⭕🔺?", "🔺, ⭕, 🔺, ⭕, 🔺, ?");
        fixed += questionRepository.updateQuestionText(
                "Complete: 2, 4, 6, ?", "2, 4, 6, ?");
        fixed += questionRepository.updateQuestionText(
                "What comes next? 🍎🍎🍊🍎🍎🍊🍎🍎?", "🍎, 🍊, 🍎, 🍊, 🍎, ?");
        if (fixed > 0) {
            log.info("Fixed {} game question(s)", fixed);
        }
    }

    private List<Topic> seedTopicsAndLessons() {
        log.info("Seeding kindergarten topics and lessons...");

        List<Lesson> allLessons = new ArrayList<>();

        // Topic 1: Counting
        Topic counting = createTopic(1, "Counting", "Learn to count from 1 to 20", "counting");
        allLessons.add(createLesson(counting, 1, "Counting 1-5", "Learn to count objects from 1 to 5", 10,
                buildLessonContent("Let's learn to count!", "Count the apples: 🍎🍎🍎", 3)));
        allLessons.add(createLesson(counting, 2, "Counting 6-10", "Count objects from 6 to 10", 10,
                buildLessonContent("Now let's count bigger numbers!", "Count the stars: ⭐⭐⭐⭐⭐⭐⭐", 7)));
        allLessons.add(createLesson(counting, 3, "Counting 11-20", "Count all the way to 20!", 15,
                buildLessonContent("You're doing great! Let's count to 20!", "Count the hearts: ❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️", 12)));
        log.info("Created topic: Counting with 3 lessons");

        // Topic 2: Number Recognition
        Topic numberRecognition = createTopic(2, "Number Recognition", "Match numbers to quantities", "numbers");
        allLessons.add(createLesson(numberRecognition, 1, "Meet the Numbers 1-5", "Recognize numbers one through five", 10,
                buildLessonContent("Let's meet the numbers!", "This is the number 3️⃣", 3)));
        allLessons.add(createLesson(numberRecognition, 2, "Meet the Numbers 6-10", "Recognize numbers six through ten", 10,
                buildLessonContent("More numbers to learn!", "This is the number 8️⃣", 8)));
        allLessons.add(createLesson(numberRecognition, 3, "Number Match Game", "Match numbers to pictures", 15,
                buildLessonContent("Let's play a matching game!", "Match 5 to: 🎈🎈🎈🎈🎈", 5)));
        log.info("Created topic: Number Recognition with 3 lessons");

        // Topic 3: Addition
        Topic addition = createTopic(3, "Addition", "Add numbers together (1+1 to 5+5)", "addition");
        allLessons.add(createLesson(addition, 1, "Adding with Pictures", "Use pictures to add", 10,
                buildLessonContent("Adding means putting things together!", "🍎🍎 + 🍎 = ?", 3)));
        allLessons.add(createLesson(addition, 2, "Adding 1 More", "What happens when we add 1?", 10,
                buildLessonContent("When we add 1, we get one more!", "3 + 1 = ?", 4)));
        allLessons.add(createLesson(addition, 3, "Adding Up to 5", "Addition facts to 5", 15,
                buildLessonContent("Let's practice adding to 5!", "2 + 3 = ?", 5)));
        log.info("Created topic: Addition with 3 lessons");

        // Topic 4: Subtraction
        Topic subtraction = createTopic(4, "Subtraction", "Take away and find the difference", "subtraction");
        allLessons.add(createLesson(subtraction, 1, "Taking Away", "What happens when we take away?", 10,
                buildLessonContent("Subtraction means taking away!", "🍪🍪🍪 - 🍪 = ?", 2)));
        allLessons.add(createLesson(subtraction, 2, "Subtraction with Objects", "Visual subtraction", 10,
                buildLessonContent("Let's see subtraction in action!", "5 bananas, eat 2: 🍌🍌🍌🍌🍌 - 🍌🍌 = ?", 3)));
        allLessons.add(createLesson(subtraction, 3, "Subtraction to 5", "Subtraction facts to 5", 15,
                buildLessonContent("Practice taking away!", "5 - 2 = ?", 3)));
        log.info("Created topic: Subtraction with 3 lessons");

        // Topic 5: Shapes
        Topic shapes = createTopic(5, "Shapes", "Learn circles, squares, triangles and more", "shapes");
        allLessons.add(createLesson(shapes, 1, "Circles and Squares", "Learn about round and square shapes", 10,
                buildLessonContent("Shapes are everywhere!", "A ⭕ is round, a ⬜ has 4 equal sides", 0)));
        allLessons.add(createLesson(shapes, 2, "Triangles and Rectangles", "Shapes with 3 and 4 sides", 10,
                buildLessonContent("More shapes to learn!", "A 🔺 has 3 sides, a rectangle has 4 sides", 0)));
        allLessons.add(createLesson(shapes, 3, "Shape Hunt", "Find shapes all around you!", 15,
                buildLessonContent("Let's find shapes!", "Can you find something shaped like a ⭕?", 0)));
        log.info("Created topic: Shapes with 3 lessons");

        // Topic 6: Comparing Numbers
        Topic comparing = createTopic(6, "Comparing Numbers", "Greater than, less than, equal to", "compare");
        allLessons.add(createLesson(comparing, 1, "Big and Small", "Compare sizes", 10,
                buildLessonContent("Let's compare things!", "🐘 is BIG, 🐭 is small", 0)));
        allLessons.add(createLesson(comparing, 2, "More and Less", "Which group has more?", 10,
                buildLessonContent("Which has more?", "🍎🍎🍎🍎🍎 or 🍎🍎? Which group has MORE?", 5)));
        allLessons.add(createLesson(comparing, 3, "Greater Than, Less Than", "Use > and < symbols", 15,
                buildLessonContent("Let's learn math symbols!", "5 > 3 means 5 is GREATER than 3", 0)));
        log.info("Created topic: Comparing Numbers with 3 lessons");

        log.info("Seeded 6 topics with 18 lessons total");

        // Seed questions for all lessons
        seedQuestions(allLessons);

        return Arrays.asList(counting, numberRecognition, addition, subtraction, shapes, comparing);
    }

    private Topic createTopic(int orderIndex, String name, String description, String iconName) {
        Topic topic = Topic.builder()
                .name(name)
                .description(description)
                .iconName(iconName)
                .orderIndex(orderIndex)
                .gradeLevel(GradeLevel.KINDERGARTEN)
                .build();
        return topicRepository.save(topic);
    }

    private Lesson createLesson(Topic topic, int orderIndex, String title, String description, 
                              int starsReward, String content) {
        Lesson lesson = Lesson.builder()
                .title(title)
                .description(description)
                .content(content)
                .orderIndex(orderIndex)
                .starsReward(starsReward)
                .topic(topic)
                .build();
        return lessonRepository.save(lesson);
    }

    private String buildLessonContent(String instructionText, String exampleText, int answer) {
        return """
                {
                  "steps": [
                    {
                      "type": "instruction",
                      "text": "%s",
                      "mascot": "leo"
                    },
                    {
                      "type": "example",
                      "text": "%s",
                      "answer": %d
                    },
                    {
                      "type": "practice",
                      "questionIds": []
                    }
                  ]
                }
                """.formatted(instructionText, exampleText, answer);
    }

    private void seedQuestions(List<Lesson> lessons) {
        if (questionRepository.count() > 0) {
            log.info("Questions already seeded, skipping...");
            return;
        }

        log.info("Seeding questions for all lessons...");
        List<Question> questions = new ArrayList<>();

        // Create a map for easy lesson lookup by title
        Lesson counting1to5 = findLessonByTitle(lessons, "Counting 1-5");
        Lesson counting6to10 = findLessonByTitle(lessons, "Counting 6-10");
        Lesson counting11to20 = findLessonByTitle(lessons, "Counting 11-20");
        Lesson numberRecognition1to5 = findLessonByTitle(lessons, "Meet the Numbers 1-5");
        Lesson numberRecognition6to10 = findLessonByTitle(lessons, "Meet the Numbers 6-10");
        Lesson numberMatchGame = findLessonByTitle(lessons, "Number Match Game");
        Lesson addingWithPictures = findLessonByTitle(lessons, "Adding with Pictures");
        Lesson adding1More = findLessonByTitle(lessons, "Adding 1 More");
        Lesson addingUpTo5 = findLessonByTitle(lessons, "Adding Up to 5");
        Lesson takingAway = findLessonByTitle(lessons, "Taking Away");
        Lesson subtractionWithObjects = findLessonByTitle(lessons, "Subtraction with Objects");
        Lesson subtractionTo5 = findLessonByTitle(lessons, "Subtraction to 5");
        Lesson circlesAndSquares = findLessonByTitle(lessons, "Circles and Squares");
        Lesson trianglesAndRectangles = findLessonByTitle(lessons, "Triangles and Rectangles");
        Lesson shapeHunt = findLessonByTitle(lessons, "Shape Hunt");
        Lesson bigAndSmall = findLessonByTitle(lessons, "Big and Small");
        Lesson moreAndLess = findLessonByTitle(lessons, "More and Less");
        Lesson greaterThanLessThan = findLessonByTitle(lessons, "Greater Than, Less Than");

        // Counting 1-5 Questions (5 questions)
        questions.add(createCountingQuestion("Count the apples: 🍎🍎", "2", Difficulty.EASY, counting1to5));
        questions.add(createCountingQuestion("Count the stars: ⭐⭐⭐⭐", "4", Difficulty.EASY, counting1to5));
        questions.add(createCountingQuestion("How many dogs? 🐕🐕🐕", "3", Difficulty.EASY, counting1to5));
        questions.add(createCountingQuestion("Count: 🌟🌟🌟🌟🌟", "5", Difficulty.EASY, counting1to5));
        questions.add(createCountingQuestion("How many birds? 🐦", "1", Difficulty.EASY, counting1to5));

        // Counting 6-10 Questions (5 questions)
        questions.add(createCountingQuestion("Count: 🔵🔵🔵🔵🔵🔵", "6", Difficulty.MEDIUM, counting6to10));
        questions.add(createCountingQuestion("How many hearts? ❤️❤️❤️❤️❤️❤️❤️", "7", Difficulty.MEDIUM, counting6to10));
        questions.add(createCountingQuestion("Count the fish: 🐟🐟🐟🐟🐟🐟🐟🐟", "8", Difficulty.MEDIUM, counting6to10));
        questions.add(createCountingQuestion("How many flowers? 🌸🌸🌸🌸🌸🌸🌸🌸🌸", "9", Difficulty.MEDIUM, counting6to10));
        questions.add(createCountingQuestion("Count: 🌙🌙🌙🌙🌙🌙🌙🌙🌙🌙", "10", Difficulty.MEDIUM, counting6to10));

        // Counting 11-20 Questions (5 questions)
        questions.add(createCountingQuestion("Count: ⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐", "11", Difficulty.MEDIUM, counting11to20));
        questions.add(createCountingQuestion("Count: 🔴🔴🔴🔴🔴🔴🔴🔴🔴🔴🔴🔴", "12", Difficulty.MEDIUM, counting11to20));
        questions.add(createCountingQuestion("Count: 🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎", "15", Difficulty.HARD, counting11to20));
        questions.add(createCountingQuestion("Count: ❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️❤️", "18", Difficulty.HARD, counting11to20));
        questions.add(createCountingQuestion("Count: 🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟🌟", "20", Difficulty.HARD, counting11to20));

        // Number Recognition 1-5 Questions (4 questions)
        // Format: "Tap the number X!" — TTS reads this aloud, child finds the numeral in the options
        questions.add(createMultipleChoiceQuestion("Tap the number three!", "[\"1\", \"2\", \"3\", \"4\"]", "3", Difficulty.EASY, numberRecognition1to5));
        questions.add(createMultipleChoiceQuestion("Tap the number five!", "[\"2\", \"5\", \"7\", \"9\"]", "5", Difficulty.EASY, numberRecognition1to5));
        questions.add(createMultipleChoiceQuestion("What number shows 🍎🍎🍎🍎?", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, numberRecognition1to5));
        questions.add(createMultipleChoiceQuestion("Tap the number two!", "[\"1\", \"2\", \"6\", \"8\"]", "2", Difficulty.EASY, numberRecognition1to5));

        // Number Recognition 6-10 Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("Tap the number eight!", "[\"6\", \"7\", \"8\", \"9\"]", "8", Difficulty.MEDIUM, numberRecognition6to10));
        questions.add(createMultipleChoiceQuestion("Tap the number seven!", "[\"5\", \"6\", \"7\", \"10\"]", "7", Difficulty.MEDIUM, numberRecognition6to10));
        questions.add(createMultipleChoiceQuestion("What number shows 🌟🌟🌟🌟🌟🌟🌟🌟🌟?", "[\"7\", \"8\", \"9\", \"10\"]", "9", Difficulty.MEDIUM, numberRecognition6to10));
        questions.add(createMultipleChoiceQuestion("Tap the number ten!", "[\"6\", \"8\", \"9\", \"10\"]", "10", Difficulty.MEDIUM, numberRecognition6to10));

        // Number Match Game Questions (3 questions)
        questions.add(createMultipleChoiceQuestion("Match the number to the picture: 🎈🎈🎈", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.EASY, numberMatchGame));
        questions.add(createMultipleChoiceQuestion("Match the number to the picture: 🐶🐶🐶🐶🐶", "[\"3\", \"4\", \"5\", \"6\"]", "5", Difficulty.MEDIUM, numberMatchGame));
        questions.add(createMultipleChoiceQuestion("Which picture matches 4?", "[\"🌸🌸\", \"🌸🌸🌸\", \"🌸🌸🌸🌸\", \"🌸🌸🌸🌸🌸\"]", "🌸🌸🌸🌸", Difficulty.MEDIUM, numberMatchGame));

        // Adding with Pictures Questions (5 questions)
        questions.add(createMultipleChoiceQuestion("1 + 1 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "2", Difficulty.EASY, addingWithPictures));
        questions.add(createMultipleChoiceQuestion("2 + 1 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.EASY, addingWithPictures));
        questions.add(createCountingQuestion("🍎 + 🍎🍎 = ?", "3", Difficulty.EASY, addingWithPictures));
        questions.add(createCountingQuestion("🌟🌟 + 🌟🌟 = ?", "4", Difficulty.EASY, addingWithPictures));
        questions.add(createCountingQuestion("🐟 + 🐟🐟🐟 = ?", "4", Difficulty.EASY, addingWithPictures));

        // Adding 1 More Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("3 + 1 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, adding1More));
        questions.add(createMultipleChoiceQuestion("4 + 1 = ?", "[\"3\", \"4\", \"5\", \"6\"]", "5", Difficulty.EASY, adding1More));
        questions.add(createMultipleChoiceQuestion("What number comes after 4?", "[\"3\", \"5\", \"6\", \"2\"]", "5", Difficulty.EASY, adding1More));
        questions.add(createMultipleChoiceQuestion("2 + 1 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "3", Difficulty.EASY, adding1More));

        // Adding Up to 5 Questions (5 questions)
        questions.add(createMultipleChoiceQuestion("2 + 2 = ?", "[\"3\", \"4\", \"5\", \"6\"]", "4", Difficulty.MEDIUM, addingUpTo5));
        questions.add(createMultipleChoiceQuestion("3 + 2 = ?", "[\"4\", \"5\", \"6\", \"7\"]", "5", Difficulty.MEDIUM, addingUpTo5));
        questions.add(createMultipleChoiceQuestion("1 + 4 = ?", "[\"3\", \"4\", \"5\", \"6\"]", "5", Difficulty.MEDIUM, addingUpTo5));
        questions.add(createMultipleChoiceQuestion("2 + 3 = ?", "[\"4\", \"5\", \"6\", \"7\"]", "5", Difficulty.MEDIUM, addingUpTo5));
        questions.add(createMultipleChoiceQuestion("3 + 1 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, addingUpTo5));

        // Taking Away Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("3 - 1 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "2", Difficulty.EASY, takingAway));
        questions.add(createCountingQuestion("🍎🍎🍎 take away 🍎 = ?", "2", Difficulty.EASY, takingAway));
        questions.add(createMultipleChoiceQuestion("2 - 1 = ?", "[\"0\", \"1\", \"2\", \"3\"]", "1", Difficulty.EASY, takingAway));
        questions.add(createCountingQuestion("🍪🍪🍪🍪 take away 🍪🍪 = ?", "2", Difficulty.EASY, takingAway));

        // Subtraction with Objects Questions (4 questions)
        questions.add(createCountingQuestion("🍌🍌🍌🍌🍌 - 🍌🍌 = ?", "3", Difficulty.EASY, subtractionWithObjects));
        questions.add(createCountingQuestion("🐟🐟🐟🐟 - 🐟 = ?", "3", Difficulty.EASY, subtractionWithObjects));
        questions.add(createMultipleChoiceQuestion("5 - 2 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.MEDIUM, subtractionWithObjects));
        questions.add(createMultipleChoiceQuestion("4 - 1 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "3", Difficulty.EASY, subtractionWithObjects));

        // Subtraction to 5 Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("4 - 2 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "2", Difficulty.MEDIUM, subtractionTo5));
        questions.add(createMultipleChoiceQuestion("5 - 3 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "2", Difficulty.MEDIUM, subtractionTo5));
        questions.add(createMultipleChoiceQuestion("5 - 4 = ?", "[\"0\", \"1\", \"2\", \"3\"]", "1", Difficulty.MEDIUM, subtractionTo5));
        questions.add(createMultipleChoiceQuestion("3 - 2 = ?", "[\"0\", \"1\", \"2\", \"3\"]", "1", Difficulty.MEDIUM, subtractionTo5));

        // Circles and Squares Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("What shape is ⭕?", "[\"Circle\", \"Square\", \"Triangle\", \"Star\"]", "Circle", Difficulty.EASY, circlesAndSquares));
        questions.add(createMultipleChoiceQuestion("What shape is ◻️?", "[\"Circle\", \"Square\", \"Triangle\", \"Rectangle\"]", "Square", Difficulty.EASY, circlesAndSquares));
        questions.add(createMultipleChoiceQuestion("A circle is ___", "[\"Round\", \"Pointy\", \"Square\", \"Flat\"]", "Round", Difficulty.EASY, circlesAndSquares));
        questions.add(createMultipleChoiceQuestion("A square has ___ equal sides", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, circlesAndSquares));

        // Triangles and Rectangles Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("A triangle has ___ sides", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.EASY, trianglesAndRectangles));
        questions.add(createMultipleChoiceQuestion("What shape is 🔺?", "[\"Circle\", \"Square\", \"Triangle\", \"Rectangle\"]", "Triangle", Difficulty.EASY, trianglesAndRectangles));
        questions.add(createMultipleChoiceQuestion("A rectangle has ___ sides", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.MEDIUM, trianglesAndRectangles));
        questions.add(createMultipleChoiceQuestion("A square has ___ corners", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.MEDIUM, trianglesAndRectangles));

        // Shape Hunt Questions (3 questions)
        questions.add(createMultipleChoiceQuestion("A clock is usually shaped like a ___", "[\"Circle\", \"Square\", \"Triangle\", \"Rectangle\"]", "Circle", Difficulty.EASY, shapeHunt));
        questions.add(createMultipleChoiceQuestion("A door is usually shaped like a ___", "[\"Circle\", \"Square\", \"Triangle\", \"Rectangle\"]", "Rectangle", Difficulty.MEDIUM, shapeHunt));
        questions.add(createMultipleChoiceQuestion("A slice of pizza looks like a ___", "[\"Circle\", \"Square\", \"Triangle\", \"Rectangle\"]", "Triangle", Difficulty.MEDIUM, shapeHunt));

        // Big and Small Questions (3 questions)
        questions.add(createMultipleChoiceQuestion("Which is bigger? 🐘 or 🐭?", "[\"🐘\", \"🐭\"]", "🐘", Difficulty.EASY, bigAndSmall));
        questions.add(createMultipleChoiceQuestion("Which is smaller? 🐕 or 🐜?", "[\"🐕\", \"🐜\"]", "🐜", Difficulty.EASY, bigAndSmall));
        questions.add(createTrueFalseQuestion("An elephant is bigger than a mouse", "True", Difficulty.EASY, bigAndSmall));

        // More and Less Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("Which is more? 🍎🍎🍎 or 🍎🍎", "[\"🍎🍎🍎\", \"🍎🍎\"]", "🍎🍎🍎", Difficulty.EASY, moreAndLess));
        questions.add(createMultipleChoiceQuestion("Which is less? 🌟🌟 or 🌟🌟🌟🌟", "[\"🌟🌟\", \"🌟🌟🌟🌟\"]", "🌟🌟", Difficulty.EASY, moreAndLess));
        questions.add(createMultipleChoiceQuestion("Which group has more? ❤️❤️❤️❤️❤️ or ❤️❤️❤️", "[\"❤️❤️❤️❤️❤️\", \"❤️❤️❤️\"]", "❤️❤️❤️❤️❤️", Difficulty.EASY, moreAndLess));
        questions.add(createMultipleChoiceQuestion("Which is less? 4 or 6?", "[\"4\", \"6\"]", "4", Difficulty.EASY, moreAndLess));

        // Greater Than, Less Than Questions (4 questions)
        questions.add(createTrueFalseQuestion("5 is greater than 3", "True", Difficulty.EASY, greaterThanLessThan));
        questions.add(createTrueFalseQuestion("2 < 7", "True", Difficulty.MEDIUM, greaterThanLessThan));
        questions.add(createTrueFalseQuestion("8 > 10", "False", Difficulty.MEDIUM, greaterThanLessThan));
        questions.add(createTrueFalseQuestion("4 = 4", "True", Difficulty.EASY, greaterThanLessThan));

        questionRepository.saveAll(questions);
        log.info("Seeded {} questions across all lessons", questions.size());
    }

    private Lesson findLessonByTitle(List<Lesson> lessons, String title) {
        return lessons.stream()
                .filter(l -> l.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + title));
    }

    private Question createCountingQuestion(String questionText, String correctAnswer, Difficulty difficulty, Lesson lesson) {
        return Question.builder()
                .questionText(questionText)
                .questionType(QuestionType.COUNTING)
                .options(null)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .lesson(lesson)
                .build();
    }

    private Question createMultipleChoiceQuestion(String questionText, String options, String correctAnswer, Difficulty difficulty, Lesson lesson) {
        return Question.builder()
                .questionText(questionText)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(options)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .lesson(lesson)
                .build();
    }

    private Question createTrueFalseQuestion(String questionText, String correctAnswer, Difficulty difficulty, Lesson lesson) {
        return Question.builder()
                .questionText(questionText)
                .questionType(QuestionType.TRUE_FALSE)
                .options("[\"True\", \"False\"]")
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .lesson(lesson)
                .build();
    }

    private void seedAchievements() {
        log.info("Seeding achievements...");

        List<AchievementData> achievements = List.of(
                new AchievementData("First Steps", "Complete your first lesson", "first_steps",
                        "{\"type\": \"lessons_completed\", \"count\": 1}", 5),
                new AchievementData("Counting Champion", "Master counting 1-20", "counting_champ",
                        "{\"type\": \"topic_completed\", \"topicId\": 1}", 20),
                new AchievementData("Addition Ace", "Complete all addition lessons", "addition_ace",
                        "{\"type\": \"topic_completed\", \"topicId\": 3}", 20),
                new AchievementData("Shape Shifter", "Master all shapes", "shape_shifter",
                        "{\"type\": \"topic_completed\", \"topicId\": 5}", 20),
                new AchievementData("Week Warrior", "Practice 7 days in a row", "week_warrior",
                        "{\"type\": \"streak\", \"days\": 7}", 50),
                new AchievementData("Star Collector", "Earn 100 stars", "star_100",
                        "{\"type\": \"stars\", \"count\": 100}", 25),
                new AchievementData("Math Explorer", "Try every topic", "explorer",
                        "{\"type\": \"topics_started\", \"count\": 6}", 30),
                new AchievementData("Super Star", "Earn 500 stars", "star_500",
                        "{\"type\": \"stars\", \"count\": 500}", 100)
        );

        for (AchievementData data : achievements) {
            Achievement achievement = Achievement.builder()
                    .name(data.name())
                    .description(data.description())
                    .badgeImageUrl(data.badge())
                    .unlockCondition(data.condition())
                    .starsBonus(data.starsBonus())
                    .build();
            achievementRepository.save(achievement);
            log.info("Created achievement: {}", data.name());
        }

        log.info("Seeded {} achievements", achievements.size());
    }

    private void seedGames(List<Topic> topics) {
        if (gameRepository.count() > 0) {
            log.info("Games already seeded, skipping...");
            return;
        }

        log.info("Seeding games and game questions...");

        // Create a map for easy topic lookup
        Map<String, Topic> topicMap = topics.stream()
                .collect(Collectors.toMap(Topic::getName, t -> t));

        Topic addition = topicMap.get("Addition");
        Topic subtraction = topicMap.get("Subtraction");
        Topic counting = topicMap.get("Counting");
        Topic shapes = topicMap.get("Shapes");
        Topic comparing = topicMap.get("Comparing Numbers");

        List<Question> gameQuestions = new ArrayList<>();

        // Game 1: Number Pop
        Game numberPop = Game.builder()
                .name("Number Pop")
                .description("Pop the balloons with the right answers!")
                .gameType(GameType.NUMBER_POP)
                .iconName("balloon")
                .baseStarsReward(15)
                .timeLimit(60)
                .topics(Arrays.asList(addition, subtraction))
                .build();
        numberPop = gameRepository.save(numberPop);
        log.info("Created game: Number Pop");

        // Number Pop questions (10)
        gameQuestions.add(createGameQuestion("3 + 2 = ?", "[\"4\", \"5\", \"6\", \"7\"]", "5", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("4 + 1 = ?", "[\"3\", \"4\", \"5\", \"6\"]", "5", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("5 - 2 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("4 - 3 = ?", "[\"0\", \"1\", \"2\", \"3\"]", "1", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("2 + 3 = ?", "[\"4\", \"5\", \"6\", \"7\"]", "5", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("6 - 2 = ?", "[\"3\", \"4\", \"5\", \"6\"]", "4", Difficulty.MEDIUM, numberPop));
        gameQuestions.add(createGameQuestion("1 + 4 = ?", "[\"3\", \"4\", \"5\", \"6\"]", "5", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("5 - 1 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, numberPop));
        gameQuestions.add(createGameQuestion("3 + 3 = ?", "[\"4\", \"5\", \"6\", \"7\"]", "6", Difficulty.MEDIUM, numberPop));
        gameQuestions.add(createGameQuestion("7 - 4 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.MEDIUM, numberPop));

        // Game 2: Counting Critters
        Game countingCritters = Game.builder()
                .name("Counting Critters")
                .description("Count the adorable animals!")
                .gameType(GameType.COUNTING_CRITTERS)
                .iconName("animals")
                .baseStarsReward(10)
                .timeLimit(90)
                .topics(Arrays.asList(counting))
                .build();
        countingCritters = gameRepository.save(countingCritters);
        log.info("Created game: Counting Critters");

        // Counting Critters questions (8)
        gameQuestions.add(createGameCountingQuestion("Count the puppies! 🐕🐕🐕🐕", "4", Difficulty.EASY, countingCritters));
        gameQuestions.add(createGameCountingQuestion("How many kittens? 🐱🐱🐱🐱🐱🐱", "6", Difficulty.MEDIUM, countingCritters));
        gameQuestions.add(createGameCountingQuestion("Count the bunnies! 🐰🐰🐰", "3", Difficulty.EASY, countingCritters));
        gameQuestions.add(createGameCountingQuestion("How many chicks? 🐤🐤🐤🐤🐤", "5", Difficulty.EASY, countingCritters));
        gameQuestions.add(createGameCountingQuestion("Count the frogs! 🐸🐸🐸🐸🐸🐸🐸", "7", Difficulty.MEDIUM, countingCritters));
        gameQuestions.add(createGameCountingQuestion("How many bears? 🐻🐻", "2", Difficulty.EASY, countingCritters));
        gameQuestions.add(createGameCountingQuestion("Count the ducks! 🦆🦆🦆🦆🦆🦆🦆🦆", "8", Difficulty.MEDIUM, countingCritters));
        gameQuestions.add(createGameCountingQuestion("How many pandas? 🐼🐼🐼🐼🐼🐼🐼🐼🐼", "9", Difficulty.HARD, countingCritters));

        // Game 3: Shape Safari
        Game shapeSafari = Game.builder()
                .name("Shape Safari")
                .description("Find shapes in the wild!")
                .gameType(GameType.SHAPE_SAFARI)
                .iconName("shapes")
                .baseStarsReward(10)
                .timeLimit(60)
                .topics(Arrays.asList(shapes))
                .build();
        shapeSafari = gameRepository.save(shapeSafari);
        log.info("Created game: Shape Safari");

        // Shape Safari questions (8) — all "find the shape" style for visual game
        gameQuestions.add(createGameQuestion("Find the circle!", "[\"Circle\", \"Square\", \"Triangle\"]", "Circle", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the triangle!", "[\"Circle\", \"Square\", \"Triangle\"]", "Triangle", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the diamond!", "[\"Diamond\", \"Circle\", \"Square\"]", "Diamond", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the square!", "[\"Circle\", \"Square\", \"Triangle\"]", "Square", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the rectangle!", "[\"Triangle\", \"Rectangle\", \"Circle\"]", "Rectangle", Difficulty.MEDIUM, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the star!", "[\"Star\", \"Circle\", \"Square\"]", "Star", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the hexagon!", "[\"Circle\", \"Square\", \"Hexagon\"]", "Hexagon", Difficulty.MEDIUM, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the oval!", "[\"Square\", \"Oval\", \"Triangle\"]", "Oval", Difficulty.EASY, shapeSafari));

        // Game 4: Math Race
        Game mathRace = Game.builder()
                .name("Math Race")
                .description("Race to solve problems fast!")
                .gameType(GameType.MATH_RACE)
                .iconName("race")
                .baseStarsReward(20)
                .timeLimit(45)
                .topics(Arrays.asList(addition, subtraction, comparing))
                .build();
        mathRace = gameRepository.save(mathRace);
        log.info("Created game: Math Race");

        // Math Race questions (10) - quick-fire mix
        gameQuestions.add(createGameQuestion("2 + 1 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.EASY, mathRace));
        gameQuestions.add(createGameQuestion("5 - 3 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "2", Difficulty.EASY, mathRace));
        gameQuestions.add(createGameQuestion("Which is bigger? 5 or 3", "[\"5\", \"3\"]", "5", Difficulty.EASY, mathRace));
        gameQuestions.add(createGameQuestion("4 + 2 = ?", "[\"5\", \"6\", \"7\", \"8\"]", "6", Difficulty.MEDIUM, mathRace));
        gameQuestions.add(createGameQuestion("6 - 4 = ?", "[\"1\", \"2\", \"3\", \"4\"]", "2", Difficulty.MEDIUM, mathRace));
        gameQuestions.add(createGameQuestion("Which is smaller? 2 or 7", "[\"2\", \"7\"]", "2", Difficulty.EASY, mathRace));
        gameQuestions.add(createGameQuestion("3 + 4 = ?", "[\"5\", \"6\", \"7\", \"8\"]", "7", Difficulty.MEDIUM, mathRace));
        gameQuestions.add(createGameQuestion("8 - 5 = ?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.HARD, mathRace));
        gameQuestions.add(createGameQuestion("Is 4 > 2?", "[\"Yes\", \"No\"]", "Yes", Difficulty.EASY, mathRace));
        gameQuestions.add(createGameQuestion("5 + 3 = ?", "[\"6\", \"7\", \"8\", \"9\"]", "8", Difficulty.HARD, mathRace));

        // Game 5: Pattern Parade
        Game patternParade = Game.builder()
                .name("Pattern Parade")
                .description("Complete the patterns!")
                .gameType(GameType.PATTERN_PARADE)
                .iconName("pattern")
                .baseStarsReward(15)
                .timeLimit(90)
                .topics(Arrays.asList(shapes, counting))
                .build();
        patternParade = gameRepository.save(patternParade);
        log.info("Created game: Pattern Parade");

        // Pattern Parade questions (6) — comma-separated items, "?" marks the blank
        gameQuestions.add(createGameQuestion("🔴, 🔵, 🔴, 🔵, 🔴, ?", "[\"🔴\", \"🔵\", \"🟢\"]", "🔵", Difficulty.EASY, patternParade));
        gameQuestions.add(createGameQuestion("⭐, 🌙, ⭐, 🌙, ⭐, ?", "[\"⭐\", \"🌙\", \"☀️\"]", "🌙", Difficulty.MEDIUM, patternParade));
        gameQuestions.add(createGameQuestion("1, 2, 3, ?, 5", "[\"3\", \"4\", \"5\", \"6\"]", "4", Difficulty.EASY, patternParade));
        gameQuestions.add(createGameQuestion("🔺, ⭕, 🔺, ⭕, 🔺, ?", "[\"🔺\", \"⭕\", \"◻️\"]", "⭕", Difficulty.EASY, patternParade));
        gameQuestions.add(createGameQuestion("2, 4, 6, ?", "[\"7\", \"8\", \"9\", \"10\"]", "8", Difficulty.HARD, patternParade));
        gameQuestions.add(createGameQuestion("🍎, 🍊, 🍎, 🍊, 🍎, ?", "[\"🍎\", \"🍊\", \"🍋\"]", "🍊", Difficulty.MEDIUM, patternParade));

        questionRepository.saveAll(gameQuestions);
        log.info("Seeded {} game questions across 5 games", gameQuestions.size());
    }

    private Question createGameQuestion(String questionText, String options, String correctAnswer, 
                                        Difficulty difficulty, Game game) {
        return Question.builder()
                .questionText(questionText)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(options)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .game(game)
                .build();
    }

    private Question createGameCountingQuestion(String questionText, String correctAnswer, 
                                                Difficulty difficulty, Game game) {
        return Question.builder()
                .questionText(questionText)
                .questionType(QuestionType.COUNTING)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .game(game)
                .build();
    }

    private void seedAvatarItems() {
        log.info("Seeding avatar items...");
        record ItemData(String name, String emoji, String tier, String type, String condition) {}

        List<ItemData> items = List.of(
            // Common items (12)
            new ItemData("Star Sticker", "🌟", "COMMON", "FRAME", "{\"type\":\"always\"}"),
            new ItemData("Rainbow Stripe", "🌈", "COMMON", "BACKGROUND", "{\"type\":\"always\"}"),
            new ItemData("Book Badge", "📚", "COMMON", "FRAME", "{\"type\":\"lessons_completed\",\"count\":3}"),
            new ItemData("Bull's Eye", "🎯", "COMMON", "FRAME", "{\"type\":\"always\"}"),
            new ItemData("Sunshine", "☀️", "COMMON", "BACKGROUND", "{\"type\":\"always\"}"),
            new ItemData("Purple Bow", "🎀", "COMMON", "HAT", "{\"type\":\"always\"}"),
            new ItemData("Baseball Cap", "🧢", "COMMON", "HAT", "{\"type\":\"lessons_completed\",\"count\":5}"),
            new ItemData("Party Blower", "🎉", "COMMON", "FRAME", "{\"type\":\"always\"}"),
            new ItemData("Paw Print", "🐾", "COMMON", "BACKGROUND", "{\"type\":\"always\"}"),
            new ItemData("Sparkle Ring", "💍", "COMMON", "FRAME", "{\"type\":\"stars\",\"count\":50}"),
            new ItemData("Music Note", "🎵", "COMMON", "BACKGROUND", "{\"type\":\"always\"}"),
            new ItemData("Daisy", "🌼", "COMMON", "HAT", "{\"type\":\"always\"}"),
            // Rare items (8)
            new ItemData("Top Hat", "🎩", "RARE", "HAT", "{\"type\":\"stars\",\"count\":200}"),
            new ItemData("Butterfly Cape", "🦋", "RARE", "CAPE", "{\"type\":\"streak\",\"days\":7}"),
            new ItemData("Crystal Ball", "🔮", "RARE", "FRAME", "{\"type\":\"stars\",\"count\":300}"),
            new ItemData("Fox Friend", "🦊", "RARE", "PET", "{\"type\":\"lessons_completed\",\"count\":10}"),
            new ItemData("Dragon Wings", "🐉", "RARE", "CAPE", "{\"type\":\"streak\",\"days\":14}"),
            new ItemData("Wizard Hat", "🧙", "RARE", "HAT", "{\"type\":\"stars\",\"count\":400}"),
            new ItemData("Turtle Pal", "🐢", "RARE", "PET", "{\"type\":\"stars\",\"count\":250}"),
            new ItemData("Streak Shield", "🛡️", "RARE", "FRAME", "{\"type\":\"streak\",\"days\":7}"),
            // Legendary items (5)
            new ItemData("Golden Crown", "👑", "LEGENDARY", "HAT", "{\"type\":\"streak\",\"days\":30}"),
            new ItemData("Speed Lightning", "⚡", "LEGENDARY", "CAPE", "{\"type\":\"stars\",\"count\":1000}"),
            new ItemData("Champion Trophy", "🏆", "LEGENDARY", "FRAME", "{\"type\":\"stars\",\"count\":500}"),
            new ItemData("Midnight Star", "🌙", "LEGENDARY", "BACKGROUND", "{\"type\":\"streak\",\"days\":100}"),
            new ItemData("Unicorn Friend", "🦄", "LEGENDARY", "PET", "{\"type\":\"stars\",\"count\":2000}")
        );

        for (ItemData item : items) {
            if (!avatarItemRepository.existsByName(item.name())) {
                AvatarItem avatarItem = AvatarItem.builder()
                        .name(item.name())
                        .emoji(item.emoji())
                        .tier(org.example.kidsmathapp.entity.enums.ItemTier.valueOf(item.tier()))
                        .itemType(org.example.kidsmathapp.entity.enums.ItemType.valueOf(item.type()))
                        .unlockCondition(item.condition())
                        .build();
                avatarItemRepository.save(avatarItem);
                log.info("Created avatar item: {} {}", item.emoji(), item.name());
            }
        }
        log.info("Avatar items seeding complete");
    }

    private record AchievementData(String name, String description, String badge,
                                   String condition, int starsBonus) {}

    private Topic createTopicForGrade(int orderIndex, String name, String description, String iconName, GradeLevel grade) {
        Topic topic = Topic.builder()
                .name(name)
                .description(description)
                .iconName(iconName)
                .orderIndex(orderIndex)
                .gradeLevel(grade)
                .build();
        return topicRepository.save(topic);
    }

    @Transactional
    void seedGrade1() {
        List<Lesson> lessons = new ArrayList<>();

        // Topic 1: Place Value
        Topic placeValue = createTopicForGrade(1, "Place Value", "Learn tens and ones", "place_value", GradeLevel.GRADE_1);
        lessons.add(createLesson(placeValue, 1, "Tens and Ones", "Understand place value with tens and ones", 15,
                buildLessonContent("Numbers have tens and ones places!", "The number 23 has 2 tens and 3 ones", 23)));
        lessons.add(createLesson(placeValue, 2, "Building Numbers", "Build 2-digit numbers", 15,
                buildLessonContent("Let's build numbers!", "3 tens + 5 ones = 35", 35)));
        lessons.add(createLesson(placeValue, 3, "Comparing 2-Digit Numbers", "Which 2-digit number is bigger?", 20,
                buildLessonContent("Compare tens first!", "45 > 38 because 4 tens > 3 tens", 0)));

        // Topic 2: Addition to 20
        Topic addition20 = createTopicForGrade(2, "Addition to 20", "Add numbers up to 20", "addition", GradeLevel.GRADE_1);
        lessons.add(createLesson(addition20, 1, "Adding to 10", "Practice addition up to 10", 15,
                buildLessonContent("Adding is putting together!", "7 + 3 = 10", 10)));
        lessons.add(createLesson(addition20, 2, "Adding to 15", "Add numbers up to 15", 15,
                buildLessonContent("Let's go higher!", "8 + 6 = 14", 14)));
        lessons.add(createLesson(addition20, 3, "Adding to 20", "Add numbers up to 20", 20,
                buildLessonContent("Almost there!", "9 + 8 = 17", 17)));

        // Topic 3: Subtraction to 20
        Topic subtraction20 = createTopicForGrade(3, "Subtraction to 20", "Subtract numbers up to 20", "subtraction", GradeLevel.GRADE_1);
        lessons.add(createLesson(subtraction20, 1, "Subtracting from 10", "Take away from 10", 15,
                buildLessonContent("Taking away is subtraction!", "10 - 4 = 6", 6)));
        lessons.add(createLesson(subtraction20, 2, "Subtracting from 15", "Subtract from 15", 15,
                buildLessonContent("Let's practice!", "15 - 7 = 8", 8)));
        lessons.add(createLesson(subtraction20, 3, "Subtracting from 20", "Subtract from 20", 20,
                buildLessonContent("You can do it!", "20 - 9 = 11", 11)));

        // Topic 4: Telling Time
        Topic time = createTopicForGrade(4, "Telling Time", "Read clocks at the hour and half-hour", "time", GradeLevel.GRADE_1);
        lessons.add(createLesson(time, 1, "Time to the Hour", "Read the hour hand", 15,
                buildLessonContent("The short hand shows the hour!", "When the short hand points to 3, it is 3 o'clock", 0)));
        lessons.add(createLesson(time, 2, "Half Past the Hour", "Read half past times", 15,
                buildLessonContent("Half past means 30 minutes!", "Half past 4 = 4:30", 0)));
        lessons.add(createLesson(time, 3, "Time Matching", "Match times to clocks", 20,
                buildLessonContent("Let's practice reading time!", "Is the clock showing 2:00 or 2:30?", 0)));

        // Topic 5: Measurement
        Topic measurement = createTopicForGrade(5, "Measurement", "Compare lengths and heights", "measurement", GradeLevel.GRADE_1);
        lessons.add(createLesson(measurement, 1, "Longer or Shorter", "Compare object lengths", 15,
                buildLessonContent("We can measure with our eyes!", "A pencil is longer than an eraser", 0)));
        lessons.add(createLesson(measurement, 2, "Taller or Shorter", "Compare heights", 15,
                buildLessonContent("Compare heights!", "A giraffe is taller than a dog", 0)));
        lessons.add(createLesson(measurement, 3, "Measuring with Units", "Use non-standard units to measure", 20,
                buildLessonContent("Use paper clips to measure!", "This book is 5 paper clips long", 0)));

        // Topic 6: Basic Fractions
        Topic fractions = createTopicForGrade(6, "Basic Fractions", "Learn halves and quarters", "fractions", GradeLevel.GRADE_1);
        lessons.add(createLesson(fractions, 1, "Halves", "Splitting into 2 equal parts", 15,
                buildLessonContent("Half means 2 equal parts!", "Cut a pizza in half: 🍕🍕", 0)));
        lessons.add(createLesson(fractions, 2, "Quarters", "Splitting into 4 equal parts", 15,
                buildLessonContent("A quarter is 1 of 4 equal parts!", "Fold paper into 4: each part is 1/4", 0)));
        lessons.add(createLesson(fractions, 3, "Halves and Quarters", "Compare halves and quarters", 20,
                buildLessonContent("2 halves = 1 whole!", "Which is bigger, 1/2 or 1/4?", 0)));

        seedGradeQuestions(lessons, GradeLevel.GRADE_1);
        log.info("Seeded Grade 1: 6 topics, 18 lessons");
    }

    @Transactional
    void seedGrade2() {
        List<Lesson> lessons = new ArrayList<>();

        // Topic 1: 3-Digit Numbers
        Topic threeDigit = createTopicForGrade(1, "3-Digit Numbers", "Understand hundreds, tens, and ones", "numbers", GradeLevel.GRADE_2);
        lessons.add(createLesson(threeDigit, 1, "Hundreds Place", "Learn the hundreds place", 15,
                buildLessonContent("Hundreds are groups of 100!", "342 has 3 hundreds, 4 tens, 2 ones", 342)));
        lessons.add(createLesson(threeDigit, 2, "Building 3-Digit Numbers", "Build and read 3-digit numbers", 15,
                buildLessonContent("Let's build big numbers!", "5 hundreds + 6 tens + 7 ones = 567", 567)));
        lessons.add(createLesson(threeDigit, 3, "Ordering 3-Digit Numbers", "Order 3-digit numbers from least to greatest", 20,
                buildLessonContent("Compare hundreds first!", "213 < 231 < 312", 0)));

        // Topic 2: Addition with Regrouping
        Topic addReg = createTopicForGrade(2, "Addition with Regrouping", "Add with carrying", "addition", GradeLevel.GRADE_2);
        lessons.add(createLesson(addReg, 1, "When to Regroup", "Learn when to carry a 1", 15,
                buildLessonContent("Regroup when ones > 9!", "17 + 8 = 25 (1 ten + 5 ones)", 25)));
        lessons.add(createLesson(addReg, 2, "Adding 2-Digit Numbers", "Add two 2-digit numbers with regrouping", 15,
                buildLessonContent("Carry the ten!", "48 + 36 = 84", 84)));
        lessons.add(createLesson(addReg, 3, "Adding 3-Digit Numbers", "Add 3-digit numbers", 20,
                buildLessonContent("Big addition!", "247 + 385 = 632", 632)));

        // Topic 3: Subtraction with Regrouping
        Topic subReg = createTopicForGrade(3, "Subtraction with Regrouping", "Subtract with borrowing", "subtraction", GradeLevel.GRADE_2);
        lessons.add(createLesson(subReg, 1, "Borrowing Basics", "Learn when to borrow", 15,
                buildLessonContent("Borrow when you need more ones!", "52 - 8 = 44", 44)));
        lessons.add(createLesson(subReg, 2, "2-Digit Subtraction", "Subtract 2-digit numbers", 15,
                buildLessonContent("Let's borrow from tens!", "75 - 38 = 37", 37)));
        lessons.add(createLesson(subReg, 3, "3-Digit Subtraction", "Subtract 3-digit numbers", 20,
                buildLessonContent("Big subtraction!", "523 - 267 = 256", 256)));

        // Topic 4: Multiplication Intro
        Topic multIntro = createTopicForGrade(4, "Multiplication Intro", "Multiply by 2s, 5s, and 10s", "multiplication", GradeLevel.GRADE_2);
        lessons.add(createLesson(multIntro, 1, "Multiply by 2", "Learn the 2 times table", 15,
                buildLessonContent("Multiplying by 2 means doubles!", "2 × 4 = 8 (4 + 4)", 8)));
        lessons.add(createLesson(multIntro, 2, "Multiply by 5", "Learn the 5 times table", 15,
                buildLessonContent("5s always end in 0 or 5!", "5 × 3 = 15", 15)));
        lessons.add(createLesson(multIntro, 3, "Multiply by 10", "Learn the 10 times table", 20,
                buildLessonContent("10s just add a zero!", "10 × 7 = 70", 70)));

        // Topic 5: Geometry
        Topic geometry = createTopicForGrade(5, "Geometry", "Learn 2D shapes and their properties", "shapes", GradeLevel.GRADE_2);
        lessons.add(createLesson(geometry, 1, "Polygons", "Learn about polygons", 15,
                buildLessonContent("A polygon has straight sides!", "Triangle: 3 sides, Square: 4 sides, Pentagon: 5 sides", 0)));
        lessons.add(createLesson(geometry, 2, "Vertices and Edges", "Count corners and sides", 15,
                buildLessonContent("Vertices are corners!", "A rectangle has 4 vertices and 4 edges", 0)));
        lessons.add(createLesson(geometry, 3, "Symmetry", "Find lines of symmetry", 20,
                buildLessonContent("Symmetry means equal halves!", "A butterfly has symmetry - both sides match", 0)));

        // Topic 6: Money
        Topic money = createTopicForGrade(6, "Money", "Learn coins and dollars", "money", GradeLevel.GRADE_2);
        lessons.add(createLesson(money, 1, "Coins", "Learn penny, nickel, dime, quarter", 15,
                buildLessonContent("Coins have values!", "Penny=1¢, Nickel=5¢, Dime=10¢, Quarter=25¢", 0)));
        lessons.add(createLesson(money, 2, "Counting Coins", "Count sets of coins", 15,
                buildLessonContent("Add the coins together!", "1 quarter + 2 dimes = 45¢", 45)));
        lessons.add(createLesson(money, 3, "Making Change", "Calculate change", 20,
                buildLessonContent("Change is what you get back!", "Pay $1 for 65¢ item = 35¢ change", 35)));

        seedGradeQuestions(lessons, GradeLevel.GRADE_2);
        log.info("Seeded Grade 2: 6 topics, 18 lessons");
    }

    @Transactional
    void seedGrade3() {
        List<Lesson> lessons = new ArrayList<>();

        // Topic 1: Multiplication
        Topic multiplication = createTopicForGrade(1, "Multiplication", "Master times tables up to 10×10", "multiplication", GradeLevel.GRADE_3);
        lessons.add(createLesson(multiplication, 1, "Times Tables 1-5", "Learn multiplication facts 1-5", 20,
                buildLessonContent("Multiplication is repeated addition!", "3 × 4 = 4 + 4 + 4 = 12", 12)));
        lessons.add(createLesson(multiplication, 2, "Times Tables 6-10", "Learn multiplication facts 6-10", 20,
                buildLessonContent("Keep practicing!", "7 × 8 = 56", 56)));
        lessons.add(createLesson(multiplication, 3, "Mixed Multiplication", "Practice all times tables", 25,
                buildLessonContent("You're a multiplication master!", "9 × 9 = 81", 81)));

        // Topic 2: Division Intro
        Topic division = createTopicForGrade(2, "Division Intro", "Learn to divide equally", "division", GradeLevel.GRADE_3);
        lessons.add(createLesson(division, 1, "Sharing Equally", "Share objects into equal groups", 20,
                buildLessonContent("Division is fair sharing!", "12 ÷ 3 = 4 (12 cookies shared by 3 friends)", 4)));
        lessons.add(createLesson(division, 2, "Division Facts", "Practice basic division", 20,
                buildLessonContent("Division undoes multiplication!", "20 ÷ 4 = 5 because 4 × 5 = 20", 5)));
        lessons.add(createLesson(division, 3, "Word Problems with Division", "Solve division word problems", 25,
                buildLessonContent("Think about equal groups!", "24 balloons for 6 kids = 24 ÷ 6 = 4 each", 4)));

        // Topic 3: Fractions
        Topic fractions3 = createTopicForGrade(3, "Fractions", "Understand 1/2, 1/3, and 1/4", "fractions", GradeLevel.GRADE_3);
        lessons.add(createLesson(fractions3, 1, "Naming Fractions", "Name fractions with numerator/denominator", 20,
                buildLessonContent("Top number = parts we have!", "3/4 means 3 out of 4 equal parts", 0)));
        lessons.add(createLesson(fractions3, 2, "Comparing Fractions", "Which fraction is larger?", 20,
                buildLessonContent("Same denominator: bigger numerator = more!", "3/4 > 1/4", 0)));
        lessons.add(createLesson(fractions3, 3, "Fractions on a Number Line", "Place fractions on a number line", 25,
                buildLessonContent("Fractions fit between 0 and 1!", "1/2 is right in the middle of 0 and 1", 0)));

        // Topic 4: Time in Minutes
        Topic timeMin = createTopicForGrade(4, "Time to the Minute", "Read clocks to the nearest minute", "time", GradeLevel.GRADE_3);
        lessons.add(createLesson(timeMin, 1, "Minutes Past the Hour", "Read minutes after the hour", 20,
                buildLessonContent("Each small mark = 1 minute!", "3:15 means 15 minutes past 3", 0)));
        lessons.add(createLesson(timeMin, 2, "Minutes to the Hour", "Read minutes before the hour", 20,
                buildLessonContent("Count minutes to next hour!", "3:45 = 15 minutes to 4", 0)));
        lessons.add(createLesson(timeMin, 3, "Elapsed Time", "Calculate time that has passed", 25,
                buildLessonContent("Subtract start from end time!", "2:00 PM to 3:30 PM = 1 hour 30 minutes", 0)));

        // Topic 5: Perimeter and Area
        Topic perimeterArea = createTopicForGrade(5, "Perimeter and Area", "Measure around and inside shapes", "measurement", GradeLevel.GRADE_3);
        lessons.add(createLesson(perimeterArea, 1, "Perimeter", "Add all sides to find perimeter", 20,
                buildLessonContent("Perimeter = add all sides!", "Square with side 4: P = 4+4+4+4 = 16", 16)));
        lessons.add(createLesson(perimeterArea, 2, "Area with Square Units", "Count squares to find area", 20,
                buildLessonContent("Area = length × width!", "Rectangle 5×3: Area = 15 square units", 15)));
        lessons.add(createLesson(perimeterArea, 3, "Perimeter vs Area", "Know the difference", 25,
                buildLessonContent("Perimeter = around, Area = inside!", "A room 4m×3m: P=14m, A=12 sq m", 0)));

        // Topic 6: Word Problems
        Topic wordProblems = createTopicForGrade(6, "Multi-Step Word Problems", "Solve problems with multiple steps", "problems", GradeLevel.GRADE_3);
        lessons.add(createLesson(wordProblems, 1, "Two-Step Problems", "Solve problems needing 2 operations", 20,
                buildLessonContent("Read carefully, solve step by step!", "Mike has 12 apples, eats 3, shares rest among 3 friends: (12-3)÷3=3", 3)));
        lessons.add(createLesson(wordProblems, 2, "Problems with Multiplication", "Word problems using multiplication", 20,
                buildLessonContent("Find the total using multiplication!", "5 bags with 8 apples each: 5×8=40", 40)));
        lessons.add(createLesson(wordProblems, 3, "Mixed Operations", "Combine +, -, ×, ÷ in one problem", 25,
                buildLessonContent("Think about what each step needs!", "Buy 3 items at $4 each, pay $20: change = 20 - (3×4) = $8", 8)));

        seedGradeQuestions(lessons, GradeLevel.GRADE_3);
        log.info("Seeded Grade 3: 6 topics, 18 lessons");
    }

    private void seedGradeQuestions(List<Lesson> lessons, GradeLevel grade) {
        List<Question> questions = new ArrayList<>();

        for (Lesson lesson : lessons) {
            // Generate 4 grade-appropriate multiple choice questions per lesson
            String title = lesson.getTitle();
            GradeLevel g = grade;

            if (g == GradeLevel.GRADE_1) {
                questions.addAll(generateGrade1Questions(lesson, title));
            } else if (g == GradeLevel.GRADE_2) {
                questions.addAll(generateGrade2Questions(lesson, title));
            } else if (g == GradeLevel.GRADE_3) {
                questions.addAll(generateGrade3Questions(lesson, title));
            }
        }

        questionRepository.saveAll(questions);
        log.info("Seeded {} questions for {} lessons in {}", questions.size(), lessons.size(), grade);
    }

    private List<Question> generateGrade1Questions(Lesson lesson, String title) {
        List<Question> qs = new ArrayList<>();
        // Addition questions
        qs.add(createMultipleChoiceQuestion("What is 8 + 7? 🍎", "[\"13\",\"14\",\"15\",\"16\"]", "15", Difficulty.EASY, lesson));
        qs.add(createMultipleChoiceQuestion("Leo has 9 🌟 and earns 6 more. How many total?", "[\"14\",\"15\",\"16\",\"17\"]", "15", Difficulty.EASY, lesson));
        qs.add(createMultipleChoiceQuestion("What is 12 + 5? 🎯", "[\"16\",\"17\",\"18\",\"19\"]", "17", Difficulty.MEDIUM, lesson));
        qs.add(createMultipleChoiceQuestion("What is 15 - 8? 🐰", "[\"6\",\"7\",\"8\",\"9\"]", "7", Difficulty.MEDIUM, lesson));
        return qs;
    }

    private List<Question> generateGrade2Questions(Lesson lesson, String title) {
        List<Question> qs = new ArrayList<>();
        qs.add(createMultipleChoiceQuestion("What is 47 + 36? 🚀", "[\"81\",\"82\",\"83\",\"84\"]", "83", Difficulty.MEDIUM, lesson));
        qs.add(createMultipleChoiceQuestion("What is 5 × 6? ⭐", "[\"28\",\"30\",\"32\",\"34\"]", "30", Difficulty.MEDIUM, lesson));
        qs.add(createMultipleChoiceQuestion("What is 82 - 45? 🎯", "[\"35\",\"36\",\"37\",\"38\"]", "37", Difficulty.MEDIUM, lesson));
        qs.add(createMultipleChoiceQuestion("How many cents in 3 dimes and 2 nickels? 💰", "[\"38\",\"39\",\"40\",\"41\"]", "40", Difficulty.HARD, lesson));
        return qs;
    }

    private List<Question> generateGrade3Questions(Lesson lesson, String title) {
        List<Question> qs = new ArrayList<>();
        qs.add(createMultipleChoiceQuestion("What is 7 × 8? ⭐", "[\"54\",\"56\",\"58\",\"60\"]", "56", Difficulty.MEDIUM, lesson));
        qs.add(createMultipleChoiceQuestion("What is 63 ÷ 7? 🎯", "[\"7\",\"8\",\"9\",\"10\"]", "9", Difficulty.MEDIUM, lesson));
        qs.add(createMultipleChoiceQuestion("A rectangle has length 6 and width 4. What is the area? 📐", "[\"20\",\"22\",\"24\",\"26\"]", "24", Difficulty.HARD, lesson));
        qs.add(createMultipleChoiceQuestion("Which fraction is bigger: 3/4 or 1/4? 🍕", "[\"3/4\",\"1/4\",\"They are equal\",\"Cannot tell\"]", "3/4", Difficulty.MEDIUM, lesson));
        return qs;
    }
}
