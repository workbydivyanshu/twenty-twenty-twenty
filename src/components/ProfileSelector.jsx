import { useState } from 'react';
import { useAppSettings } from '../contexts/SettingsContext';

const PROFILE_COLORS = [
  '#818cf8', '#34d399', '#fbbf24', '#f87171',
  '#a78bfa', '#60a5fa', '#fb923c', '#e879f9',
];

const PROFILE_ICONS = {
  briefcase: <path d="M20 7H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2z" />,
  user: <><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" /></>,
  book: <><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z" /></>,
  coffee: <><path d="M17 8h1a4 4 0 010 8h-1"/><path d="M3 8h14v9a4 4 0 01-4 4H7a4 4 0 01-4-4V8z"/><path d="M6 1v3M10 1v3M14 1v3" /></>,
  code: <><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18" /></>,
  heart: <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />,
  star: <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />,
  circle: <circle cx="12" cy="12" r="8" fill="currentColor" />,
};

const ICON_NAMES = Object.keys(PROFILE_ICONS);

export default function ProfileSelector({ onClose }) {
  const { profiles, settings, setActiveProfile, addProfile, deleteProfile } = useAppSettings();
  const [showAddForm, setShowAddForm] = useState(false);
  const [newName, setNewName] = useState('');
  const [newColor, setNewColor] = useState(PROFILE_COLORS[0]);
  const [newIcon, setNewIcon] = useState('circle');
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const handleSelect = (profileId) => {
    setActiveProfile(profileId);
    onClose();
  };

  const handleAdd = () => {
    if (!newName.trim()) return;
    addProfile(newName.trim(), newColor, newIcon);
    setNewName('');
    setShowAddForm(false);
  };

  const handleDelete = (profileId) => {
    if (deleteConfirm === profileId) {
      deleteProfile(profileId);
      setDeleteConfirm(null);
    } else {
      setDeleteConfirm(profileId);
    }
  };

  return (
    <div className="onboarding-overlay" onClick={onClose}>
      <div className="profile-selector-card" onClick={e => e.stopPropagation()}>
        <div className="profile-selector-header">
          <div className="profile-selector-title">Profiles</div>
          <button className="profile-selector-close" onClick={onClose}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="profile-selector-list">
          {profiles.map(profile => (
            <div
              key={profile.id}
              className={`profile-item ${settings.activeProfileId === profile.id ? 'active' : ''}`}
            >
              <button
                className="profile-item-main"
                onClick={() => handleSelect(profile.id)}
                style={{ '--profile-color': profile.color }}
              >
                <div className="profile-icon" style={{ color: profile.color }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="18" height="18">
                    {PROFILE_ICONS[profile.icon] || PROFILE_ICONS.circle}
                  </svg>
                </div>
                <span className="profile-name">{profile.name}</span>
                {settings.activeProfileId === profile.id && (
                  <div className="profile-check">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" width="16" height="16">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  </div>
                )}
              </button>
              {profiles.length > 1 && (
                <button
                  className={`profile-delete ${deleteConfirm === profile.id ? 'confirm' : ''}`}
                  onClick={() => handleDelete(profile.id)}
                  title={deleteConfirm === profile.id ? 'Click again to confirm delete' : 'Delete profile'}
                >
                  {deleteConfirm === profile.id ? (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="14" height="14">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="14" height="14">
                      <polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14H6L5 6" /><path d="M10 11v6M14 11v6" /><path d="M9 6V4h6v2" />
                    </svg>
                  )}
                </button>
              )}
            </div>
          ))}
        </div>

        {showAddForm ? (
          <div className="profile-add-form">
            <input
              className="profile-add-input"
              type="text"
              placeholder="Profile name"
              value={newName}
              onChange={e => setNewName(e.target.value)}
              maxLength={20}
              autoFocus
            />
            <div className="profile-add-options">
              <div className="profile-color-picker">
                {PROFILE_COLORS.map(color => (
                  <button
                    key={color}
                    className={`profile-color-dot ${newColor === color ? 'selected' : ''}`}
                    style={{ background: color }}
                    onClick={() => setNewColor(color)}
                  />
                ))}
              </div>
              <div className="profile-icon-picker">
                {ICON_NAMES.map(iconName => (
                  <button
                    key={iconName}
                    className={`profile-icon-btn ${newIcon === iconName ? 'selected' : ''}`}
                    onClick={() => setNewIcon(iconName)}
                    style={{ color: newColor }}
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="16" height="16">
                      {PROFILE_ICONS[iconName]}
                    </svg>
                  </button>
                ))}
              </div>
            </div>
            <div className="profile-add-actions">
              <button className="btn-ghost-sm" onClick={() => setShowAddForm(false)}>Cancel</button>
              <button className="btn-accent-sm" onClick={handleAdd} disabled={!newName.trim()}>Add Profile</button>
            </div>
          </div>
        ) : (
          <button className="profile-add-btn" onClick={() => setShowAddForm(true)}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" width="16" height="16">
              <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            Add Profile
          </button>
        )}
      </div>
    </div>
  );
}
