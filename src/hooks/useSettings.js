import { useState, useCallback } from 'react';
import { SETTINGS_KEY } from '../utils/constants';
import { getItem, setItem, generateId } from '../utils/storage';

const DEFAULTS = {
  notificationsEnabled: true,
  soundEnabled: true,
  volume: 0.7,
  onboardingComplete: false,
  notificationPermission: 'default',
  activeProfileId: 'default',
};

const DEFAULT_PROFILES = [
  { id: 'default', name: 'Work', color: '#818cf8', icon: 'briefcase' },
  { id: 'personal', name: 'Personal', color: '#34d399', icon: 'user' },
  { id: 'study', name: 'Study', color: '#fbbf24', icon: 'book' },
];

export function useSettings() {
  const [settings, setSettings] = useState(() => {
    return getItem(SETTINGS_KEY, DEFAULTS);
  });

  const [profiles, setProfiles] = useState(() => {
    return getItem('twenty-v2-profiles', DEFAULT_PROFILES);
  });

  const updateSetting = useCallback((key, value) => {
    setSettings(prev => {
      const next = { ...prev, [key]: value };
      setItem(SETTINGS_KEY, next);
      return next;
    });
  }, []);

  const getActiveProfile = useCallback(() => {
    return profiles.find(p => p.id === settings.activeProfileId) || profiles[0];
  }, [profiles, settings.activeProfileId]);

  const setActiveProfile = useCallback((profileId) => {
    updateSetting('activeProfileId', profileId);
  }, [updateSetting]);

  const addProfile = useCallback((name, color, icon) => {
    const newProfile = {
      id: generateId(),
      name,
      color,
      icon: icon || 'circle',
    };
    setProfiles(prev => {
      const next = [...prev, newProfile];
      setItem('twenty-v2-profiles', next);
      return next;
    });
    return newProfile;
  }, []);

  const updateProfile = useCallback((id, updates) => {
    setProfiles(prev => {
      const next = prev.map(p => p.id === id ? { ...p, ...updates } : p);
      setItem('twenty-v2-profiles', next);
      return next;
    });
  }, []);

  const deleteProfile = useCallback((id) => {
    if (profiles.length <= 1) return;
    setProfiles(prev => {
      const next = prev.filter(p => p.id !== id);
      setItem('twenty-v2-profiles', next);
      return next;
    });
    if (settings.activeProfileId === id) {
      updateSetting('activeProfileId', profiles.filter(p => p.id !== id)[0]?.id || 'default');
    }
  }, [profiles, settings.activeProfileId, updateSetting]);

  const requestNotificationPermission = useCallback(async () => {
    if (!('Notification' in window)) return 'unsupported';

    if (Notification.permission === 'granted') {
      updateSetting('notificationPermission', 'granted');
      updateSetting('notificationsEnabled', true);
      return 'granted';
    }

    if (Notification.permission === 'denied') {
      updateSetting('notificationPermission', 'denied');
      return 'denied';
    }

    try {
      const perm = await Notification.requestPermission();
      updateSetting('notificationPermission', perm);
      if (perm === 'granted') {
        updateSetting('notificationsEnabled', true);
      }
      return perm;
    } catch {
      return 'error';
    }
  }, [updateSetting]);

  const checkNotificationPermission = useCallback(() => {
    if (!('Notification' in window)) return 'unsupported';
    const perm = Notification.permission;
    updateSetting('notificationPermission', perm);
    return perm;
  }, [updateSetting]);

  const resetSettings = useCallback(() => {
    setSettings(DEFAULTS);
    setItem(SETTINGS_KEY, DEFAULTS);
  }, []);

  const exportData = useCallback(() => {
    const data = getItem(SETTINGS_KEY, {});
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `twenty-settings-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }, []);

  return {
    settings,
    profiles,
    getActiveProfile,
    setActiveProfile,
    addProfile,
    updateProfile,
    deleteProfile,
    updateSetting,
    requestNotificationPermission,
    checkNotificationPermission,
    resetSettings,
    exportData,
  };
}
