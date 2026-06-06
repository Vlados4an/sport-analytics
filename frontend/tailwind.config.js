/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'monospace'],
      },
      colors: {
        brand: {
          50: '#f3f0ff',
          100: '#e9e3ff',
          200: '#d4c9fe',
          300: '#b49ffc',
          400: '#9171f8',
          500: '#7048e8',
          600: '#5f35d2',
          700: '#4f2cb0',
          800: '#3d2287',
          900: '#2d1a65',
        },
        ink: {
          900: '#1a1a2e',
          800: '#23233d',
          700: '#2f2f4a',
          600: '#4a4a6a',
          500: '#6b6b8a',
          400: '#9090aa',
          300: '#b4b4c8',
          200: '#d5d5e2',
          100: '#ebebf0',
          50: '#f5f5f8',
        },
      },
      borderRadius: {
        '2xl': '16px',
        '3xl': '20px',
      },
      boxShadow: {
        card: '0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03)',
        elevated: '0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04)',
        glow: '0 0 20px rgba(112, 72, 232, 0.15)',
      },
    },
  },
  plugins: [],
}
