import { useState } from 'react';
import { useAppSettings } from '../contexts/SettingsContext';

export default function Onboarding({ onComplete }) {
  const [step, setStep] = useState(0);
  const { requestNotificationPermission, updateSetting } = useAppSettings();

  const totalSteps = 3;

  const handleNext = async () => {
    if (step === 1) {
      await requestNotificationPermission();
    }

    if (step < totalSteps - 1) {
      setStep(step + 1);
    } else {
      updateSetting('onboardingComplete', true);
      onComplete();
    }
  };

  const handleSkip = () => {
    updateSetting('onboardingComplete', true);
    onComplete();
  };

  return (
    <div className="onboarding-overlay">
      <div className="onboarding-card">
        <div className="onboarding-step">
          {step === 0 && (
            <>
              <div className="onboarding-illustration">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
              </div>
              <div className="onboarding-title">Protect your eyes</div>
              <div className="onboarding-text">
                Twenty helps you follow the 20-20-20 rule to reduce digital eye strain. Every 20 minutes, take a 20-second break to look at something 20 feet away.
              </div>
              <div className="onboarding-rule">
                <div className="onboarding-rule-item">
                  <div className="onboarding-rule-num">1</div>
                  <span>Every <strong>20 minutes</strong> of screen time</span>
                </div>
                <div className="onboarding-rule-item">
                  <div className="onboarding-rule-num">2</div>
                  <span>Look at something <strong>20 feet</strong> away</span>
                </div>
                <div className="onboarding-rule-item">
                  <div className="onboarding-rule-num">3</div>
                  <span>For <strong>20 seconds</strong></span>
                </div>
              </div>
            </>
          )}

          {step === 1 && (
            <>
              <div className="onboarding-illustration">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
              </div>
              <div className="onboarding-title">Stay on track</div>
              <div className="onboarding-text">
                Enable notifications and we'll remind you when it's time to take a break. You can always change this later in settings.
              </div>
              <div className="onboarding-text" style={{ fontSize: '13px', opacity: 0.7 }}>
                We only send break reminders. No spam, ever.
              </div>
            </>
          )}

          {step === 2 && (
            <>
              <div className="onboarding-illustration">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </div>
              <div className="onboarding-title">You're all set</div>
              <div className="onboarding-text">
                Start your first session and take the first step toward healthier screen habits. Your eyes will thank you.
              </div>
            </>
          )}

          <div className="onboarding-dots">
            {[0, 1, 2].map(i => (
              <div key={i} className={`onboarding-dot ${i === step ? 'active' : ''}`} />
            ))}
          </div>

          <div className="onboarding-actions">
            <button className="btn-primary" onClick={handleNext} style={{ width: '100%', justifyContent: 'center' }}>
              {step === totalSteps - 1 ? "Let's go" : step === 1 ? 'Enable & Continue' : 'Continue'}
            </button>
            {step < totalSteps - 1 && (
              <button className="onboarding-skip" onClick={handleSkip}>
                Skip for now
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
