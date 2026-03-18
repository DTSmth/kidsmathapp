package org.example.kidsmathapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.entity.Achievement;
import org.example.kidsmathapp.entity.Game;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.entity.Topic;
import org.example.kidsmathapp.entity.enums.Difficulty;
import org.example.kidsmathapp.entity.enums.GameType;
import org.example.kidsmathapp.entity.enums.GradeLevel;
import org.example.kidsmathapp.entity.enums.QuestionType;
import org.example.kidsmathapp.repository.AchievementRepository;
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
            log.info("Database already contains data, skipping seeding.");
            // Still seed games if they don't exist
            if (gameRepository.count() == 0) {
                log.info("Seeding games...");
                List<Topic> topics = topicRepository.findAll();
                seedGames(topics);
                log.info("Game seeding completed!");
            }
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
        questions.add(createMultipleChoiceQuestion("Which number is this? 3", "[\"1\", \"2\", \"3\", \"4\"]", "3", Difficulty.EASY, numberRecognition1to5));
        questions.add(createMultipleChoiceQuestion("Tap the number five", "[\"2\", \"5\", \"7\", \"9\"]", "5", Difficulty.EASY, numberRecognition1to5));
        questions.add(createMultipleChoiceQuestion("What number shows 🍎🍎🍎🍎?", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, numberRecognition1to5));
        questions.add(createMultipleChoiceQuestion("Which is the number 2?", "[\"1\", \"2\", \"6\", \"8\"]", "2", Difficulty.EASY, numberRecognition1to5));

        // Number Recognition 6-10 Questions (4 questions)
        questions.add(createMultipleChoiceQuestion("Which number is this? 8", "[\"6\", \"7\", \"8\", \"9\"]", "8", Difficulty.MEDIUM, numberRecognition6to10));
        questions.add(createMultipleChoiceQuestion("Tap the number seven", "[\"5\", \"6\", \"7\", \"10\"]", "7", Difficulty.MEDIUM, numberRecognition6to10));
        questions.add(createMultipleChoiceQuestion("What number shows 🌟🌟🌟🌟🌟🌟🌟🌟🌟?", "[\"7\", \"8\", \"9\", \"10\"]", "9", Difficulty.MEDIUM, numberRecognition6to10));
        questions.add(createMultipleChoiceQuestion("Which is the number 10?", "[\"6\", \"8\", \"9\", \"10\"]", "10", Difficulty.MEDIUM, numberRecognition6to10));

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

        // Shape Safari questions (8)
        gameQuestions.add(createGameQuestion("Find the circle! ⭕◻️🔺", "[\"Circle\", \"Square\", \"Triangle\"]", "Circle", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("What shape has 3 sides?", "[\"Circle\", \"Square\", \"Triangle\", \"Rectangle\"]", "Triangle", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("How many corners does a square have?", "[\"2\", \"3\", \"4\", \"5\"]", "4", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Which shape is round?", "[\"Triangle\", \"Square\", \"Circle\", \"Rectangle\"]", "Circle", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("A door is shaped like a...", "[\"Circle\", \"Triangle\", \"Rectangle\", \"Star\"]", "Rectangle", Difficulty.MEDIUM, shapeSafari));
        gameQuestions.add(createGameQuestion("How many sides does a triangle have?", "[\"2\", \"3\", \"4\", \"5\"]", "3", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("Find the square! ⭕◻️🔺", "[\"Circle\", \"Square\", \"Triangle\"]", "Square", Difficulty.EASY, shapeSafari));
        gameQuestions.add(createGameQuestion("A wheel is shaped like a...", "[\"Square\", \"Triangle\", \"Circle\", \"Rectangle\"]", "Circle", Difficulty.MEDIUM, shapeSafari));

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

        // Pattern Parade questions (6)
        gameQuestions.add(createGameQuestion("What comes next? 🔴🔵🔴🔵🔴?", "[\"🔴\", \"🔵\", \"🟢\"]", "🔵", Difficulty.EASY, patternParade));
        gameQuestions.add(createGameQuestion("Complete: ⭐⭐🌙⭐⭐🌙⭐⭐?", "[\"⭐\", \"🌙\", \"☀️\"]", "🌙", Difficulty.MEDIUM, patternParade));
        gameQuestions.add(createGameQuestion("What's missing? 1, 2, 3, ?, 5", "[\"3\", \"4\", \"5\", \"6\"]", "4", Difficulty.EASY, patternParade));
        gameQuestions.add(createGameQuestion("What comes next? 🔺⭕🔺⭕🔺?", "[\"🔺\", \"⭕\", \"◻️\"]", "⭕", Difficulty.EASY, patternParade));
        gameQuestions.add(createGameQuestion("Complete: 2, 4, 6, ?", "[\"7\", \"8\", \"9\", \"10\"]", "8", Difficulty.HARD, patternParade));
        gameQuestions.add(createGameQuestion("What comes next? 🍎🍎🍊🍎🍎🍊🍎🍎?", "[\"🍎\", \"🍊\", \"🍋\"]", "🍊", Difficulty.MEDIUM, patternParade));

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

    private record AchievementData(String name, String description, String badge, 
                                   String condition, int starsBonus) {}
}
