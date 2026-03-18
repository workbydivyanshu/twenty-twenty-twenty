export default function SessionBadge({ state }) {
  let label = 'Ready';
  let cls = '';

  if (state === 'active') {
    label = 'Active Session';
    cls = 'active';
  } else if (state === 'break') {
    label = 'Taking a Break';
    cls = 'break';
  }

  return (
    <div className={`session-badge ${cls}`} aria-live="polite">
      <div className="badge-dot" />
      {label}
    </div>
  );
}
