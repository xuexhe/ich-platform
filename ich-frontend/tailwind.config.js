/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'ich-dark': '#1A365D',
        'ich-red': '#C92C2C',
        'ich-gold': '#D4AF37',
        'ich-bg': '#F5F5DC',
        'ich-paper': '#F9F6F0',
      },
      fontFamily: {
        'serif': ['Noto Serif SC', 'serif'],
      },
      screens: {
        'xs': '480px',
        'sm': '640px',
        'md': '768px',
        'lg': '1024px',
        'xl': '1280px',
        '2xl': '1536px',
      }
    },
  },
  plugins: [],
}