import { useCallback, useRef } from 'react';

export function useNotifications(settings) {
  const swRef = useRef(null);

  const registerServiceWorker = useCallback(async () => {
    if (!('serviceWorker' in navigator)) return null;
    try {
      const reg = await navigator.serviceWorker.register('/sw.js');
      swRef.current = reg;
      return reg;
    } catch {
      return null;
    }
  }, []);

  const showNotification = useCallback(async (title, options = {}) => {
    if (!settings.notificationsEnabled) return;
    if (!('Notification' in window)) return;
    if (Notification.permission !== 'granted') return;

    const defaultOptions = {
      icon: '/favicon.svg',
      badge: '/favicon.svg',
      tag: 'twenty-break',
      renotify: true,
      requireInteraction: false,
      silent: false,
      ...options,
    };

    if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
      try {
        const reg = swRef.current || await navigator.serviceWorker.ready;
        reg.showNotification(title, defaultOptions);
        return;
      } catch {
        /* SW notification fallback to Notification constructor */
      }
    }

    new Notification(title, defaultOptions);
  }, [settings.notificationsEnabled]);

  const triggerBreakNotification = useCallback(async () => {
    await showNotification('Time for a break', {
      body: 'Look at something 20 feet away for 20 seconds.',
      tag: 'twenty-break',
      requireInteraction: true,
    });
  }, [showNotification]);

  return {
    registerServiceWorker,
    showNotification,
    triggerBreakNotification,
  };
}
