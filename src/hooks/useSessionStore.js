import { useState, useCallback } from 'react';
import { SESSIONS_KEY } from '../utils/constants';
import { getItem, setItem, generateId } from '../utils/storage';
import { getWeekStart, getMonthStart, getYearStart } from '../utils/dateUtils';

export function useSessionStore(activeProfileId = 'all') {
  const [sessions, setSessions] = useState(() => getItem(SESSIONS_KEY, []));

  const getFiltered = useCallback((filter) => {
    const now = new Date();
    let start;

    switch (filter) {
      case 'week':
        start = getWeekStart(now);
        break;
      case 'month':
        start = getMonthStart(now);
        break;
      case 'year':
        start = getYearStart(now);
        break;
      default:
        start = new Date(0);
    }

    let result = sessions.filter(s => new Date(s.startTime) >= start);

    if (activeProfileId !== 'all') {
      result = result.filter(s => s.profileId === activeProfileId);
    }

    return result;
  }, [sessions, activeProfileId]);

  const getPreviousPeriod = useCallback((filter) => {
    const now = new Date();
    let periodStart, periodEnd;

    switch (filter) {
      case 'week': {
        const thisWeekStart = getWeekStart(now);
        const lastWeekEnd = new Date(thisWeekStart);
        lastWeekEnd.setDate(lastWeekEnd.getDate() - 1);
        const lastWeekStart = getWeekStart(lastWeekEnd);
        periodStart = lastWeekStart;
        periodEnd = lastWeekEnd;
        break;
      }
      case 'month': {
        const thisMonthStart = getMonthStart(now);
        const lastMonthEnd = new Date(thisMonthStart);
        lastMonthEnd.setDate(lastMonthEnd.getDate() - 1);
        const lastMonthStart = new Date(lastMonthEnd.getFullYear(), lastMonthEnd.getMonth(), 1);
        periodStart = lastMonthStart;
        periodEnd = lastMonthEnd;
        break;
      }
      default:
        return [];
    }

    let result = sessions.filter(s => {
      const d = new Date(s.startTime);
      return d >= periodStart && d <= periodEnd;
    });

    if (activeProfileId !== 'all') {
      result = result.filter(s => s.profileId === activeProfileId);
    }

    return result;
  }, [sessions, activeProfileId]);

  const computeStreak = (sessionList) => {
    if (sessionList.length === 0) return 0;

    const uniqueDays = new Set(
      sessionList.map(s => new Date(s.startTime).toDateString())
    );

    let streak = 0;
    const today = new Date();

    for (let i = 0; i < 365; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() - i);
      if (uniqueDays.has(d.toDateString())) {
        streak++;
      } else {
        break;
      }
    }

    return streak;
  };

  const computeStats = useCallback((filteredSessions) => {
    if (filteredSessions.length === 0) return null;

    const totalTime = filteredSessions.reduce((sum, s) => sum + s.durationMs, 0);
    const totalBreaksTaken = filteredSessions.reduce((sum, s) => sum + s.breaksTaken, 0);
    const totalBreaksSkipped = filteredSessions.reduce((sum, s) => sum + s.breaksSkipped, 0);
    const totalBreaksTriggered = filteredSessions.reduce((sum, s) => sum + s.breaksTriggered, 0);
    const avgDuration = totalTime / filteredSessions.length;
    const longestSession = Math.max(...filteredSessions.map(s => s.durationMs));

    const totalBreaks = totalBreaksTaken + totalBreaksSkipped;
    const complianceRate = totalBreaks > 0 ? totalBreaksTaken / (totalBreaksTriggered || 1) : 0;

    const streakDays = computeStreak(filteredSessions);

    return {
      count: filteredSessions.length,
      totalTime,
      totalBreaksTaken,
      totalBreaksSkipped,
      totalBreaksTriggered,
      avgDuration,
      longestSession,
      complianceRate,
      streakDays,
    };
  }, []);

  const getStreakCalendar = useCallback((weeks = 12) => {
    const today = new Date();
    const days = [];
    const sessionDays = new Set(
      sessions
        .filter(s => activeProfileId === 'all' || s.profileId === activeProfileId)
        .map(s => new Date(s.startTime).toDateString())
    );

    const startDay = new Date(today);
    startDay.setDate(today.getDate() - (weeks * 7) + 1);

    for (let i = 0; i < weeks * 7; i++) {
      const d = new Date(startDay);
      d.setDate(startDay.getDate() + i);
      const dateStr = d.toDateString();
      const hasSession = sessionDays.has(dateStr);
      const dayOfWeek = d.getDay();
      days.push({
        date: d,
        dateStr,
        hasSession,
        isToday: dateStr === today.toDateString(),
        isWeekStart: dayOfWeek === 0,
        dayOfWeek,
      });
    }

    return days;
  }, [sessions, activeProfileId]);

  const getBestDayOfWeek = useCallback(() => {
    const dayCounts = [0, 0, 0, 0, 0, 0, 0];
    const dayTimes = [0, 0, 0, 0, 0, 0, 0];

    sessions
      .filter(s => activeProfileId === 'all' || s.profileId === activeProfileId)
      .forEach(s => {
        const day = new Date(s.startTime).getDay();
        dayCounts[day]++;
        dayTimes[day] += s.durationMs;
      });

    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    let bestDayIndex = 0;
    let maxCount = 0;
    dayCounts.forEach((count, i) => {
      if (count > maxCount) {
        maxCount = count;
        bestDayIndex = i;
      }
    });

    if (maxCount === 0) return null;

    return {
      day: dayNames[bestDayIndex],
      shortDay: dayNames[bestDayIndex].slice(0, 3),
      count: maxCount,
      totalTime: dayTimes[bestDayIndex],
      dayIndex: bestDayIndex,
      allDays: dayNames.map((name, i) => ({
        name,
        shortName: name.slice(0, 3),
        count: dayCounts[i],
        totalTime: dayTimes[i],
      })),
    };
  }, [sessions, activeProfileId]);

  const saveSession = useCallback((session, profileId = 'default') => {
    const newSession = {
      id: generateId(),
      profileId,
      startTime: session.startTime,
      endTime: session.endTime,
      durationMs: session.durationMs,
      breaksTriggered: session.breaksTriggered || 0,
      breaksTaken: session.breaksTaken || 0,
      breaksSkipped: session.breaksSkipped || 0,
      complianceRate: session.durationMs > 0
        ? (session.breaksTaken || 0) / (session.breaksTriggered || 1)
        : 0,
    };

    setSessions(prev => {
      const updated = [newSession, ...prev];
      setItem(SESSIONS_KEY, updated);
      return updated;
    });

    return newSession;
  }, []);

  const clearAllSessions = useCallback((profileId = 'all') => {
    setSessions(prev => {
      const updated = profileId === 'all'
        ? []
        : prev.filter(s => s.profileId !== profileId);
      setItem(SESSIONS_KEY, updated);
      return updated;
    });
  }, []);

  const exportSessions = useCallback(() => {
    const blob = new Blob([JSON.stringify(sessions, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `twenty-sessions-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }, [sessions]);

  return {
    sessions,
    saveSession,
    clearAllSessions,
    exportSessions,
    getFiltered,
    getPreviousPeriod,
    computeStats,
    getStreakCalendar,
    getBestDayOfWeek,
  };
}
