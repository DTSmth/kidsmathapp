interface Props {
  storyContext: string;
}

// Extract an emoji scene from the story text (first 1-2 emoji chars)
const extractEmoji = (text: string): string => {
  const match = text.match(/[\p{Emoji_Presentation}\p{Extended_Pictographic}]/gu);
  return match ? match.slice(0, 3).join(' ') : '📖';
};

const StoryModeQuestion = ({ storyContext }: Props) => {
  const sceneEmoji = extractEmoji(storyContext);

  return (
    <div
      className="rounded-2xl border-2 border-dashed border-accent bg-background px-5 py-4 mb-3"
      role="region"
      aria-label="Story context"
    >
      {/* Scene */}
      <div className="flex justify-center mb-3">
        <span className="text-4xl">{sceneEmoji}</span>
      </div>
      {/* Story text */}
      <p className="text-sm text-gray-700 italic text-center leading-relaxed">{storyContext}</p>
      {/* Divider */}
      <div className="mt-3 flex items-center gap-2">
        <div className="flex-1 h-px bg-accent/40" />
        <span className="text-xs font-semibold text-gray-400">Now solve it!</span>
        <div className="flex-1 h-px bg-accent/40" />
      </div>
    </div>
  );
};

export default StoryModeQuestion;
