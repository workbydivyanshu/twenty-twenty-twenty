import { createContext, useContext } from 'react';
import { useSettings } from '../hooks/useSettings';
import { useSound } from '../hooks/useSound';

const SettingsContext = createContext(null);

export function SettingsProvider({ children }) {
  const settingsHook = useSettings();
  const sound = useSound();

  return (
    <SettingsContext.Provider value={{ ...settingsHook, sound }}>
      {children}
    </SettingsContext.Provider>
  );
}

export function useAppSettings() {
  const ctx = useContext(SettingsContext);
  if (!ctx) throw new Error('useAppSettings must be used within SettingsProvider');
  return ctx;
}
