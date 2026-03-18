import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('http://localhost:8080/api/v1/topics', () => {
    return HttpResponse.json({
      success: true,
      data: [
        {
          id: 1,
          name: 'Counting',
          description: 'Learn to count',
          iconName: 'counting',
          lessonsCompleted: 2,
          totalLessons: 3,
          progressPercent: 67,
          isUnlocked: true,
        },
        {
          id: 2,
          name: 'Addition',
          description: 'Learn to add',
          iconName: 'addition',
          lessonsCompleted: 0,
          totalLessons: 5,
          progressPercent: 0,
          isUnlocked: false,
        },
      ],
    });
  }),
  http.get('http://localhost:8080/api/v1/progress/dashboard/:childId', () => {
    return HttpResponse.json({
      success: true,
      data: {
        childId: 1,
        childName: 'Emma',
        totalStars: 20,
        currentStreak: 3,
        topics: [],
        recentAchievements: [],
        dailyChallengeComplete: true,
      },
    });
  }),
  http.post('http://localhost:8080/api/v1/questions/:id/check', () => {
    return HttpResponse.json({
      success: true,
      data: {
        questionId: 1,
        correct: true,
        correctAnswer: '7',
        message: 'Great job!',
      },
    });
  }),
];
