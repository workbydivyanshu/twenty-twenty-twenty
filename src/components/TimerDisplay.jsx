import { formatDuration } from '../utils/dateUtils';

export default function TimerDisplay({ elapsed }) {
  return (
    <div className="timer-display" role="timer" aria-live="polite" aria-label="Session timer">
      {formatDuration(elapsed)}
    </div>
  );
}
