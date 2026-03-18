export default function StartEndButton({ isRunning, onStart, onEnd }) {
  if (isRunning) {
    return (
      <button className="btn-secondary" onClick={onEnd}>
        End Session
      </button>
    );
  }

  return (
    <button className="btn-primary" onClick={onStart}>
      Start Session
    </button>
  );
}
