<script setup>
defineProps({ name: String, size: { type: [Number, String], default: 20 } })
const paths = {
  arrow: 'M5 12h14m-6-6 6 6-6 6',
  chevron: 'm9 5 7 7-7 7',
  down: 'm6 9 6 6 6-6',
  download: 'M12 3v12m-5-5 5 5 5-5M4 15v5h16v-5',
  shield: 'm12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6l8-3Zm-4 9 3 3 5-6',
  layers: 'm12 3 10 6-10 6L2 9l10-6ZM2 14l10 6 10-6M2 18l10 6 10-6',
  grid: 'M3 3h7v7H3V3Zm11 0h7v7h-7V3ZM3 14h7v7H3v-7Zm11 0h7v7h-7v-7Z',
  users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2m20 0v-2a4 4 0 0 0-3-3.87M9 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm7 0a4 4 0 0 1 0 8',
  globe: 'M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM3 12h18M12 3c5 5 5 13 0 18-5-5-5-13 0-18Z',
  sparkles: 'm12 3 2.7 6.3L21 12l-6.3 2.7L12 21l-2.7-6.3L3 12l6.3-2.7L12 3ZM20 2v4m-2-2h4',
  check: 'm5 12 4 4L19 6',
  close: 'm6 6 12 12M6 18 18 6',
  search: 'M10.5 3a7.5 7.5 0 1 0 0 15 7.5 7.5 0 0 0 0-15ZM16 16l5 5',
  book: 'M12 6C9 3 5 3 2 4v15c4-1 7-1 10 2m0-15c3-3 7-3 10-2v15c-4-1-7-1-10 2V6Z',
  message: 'M21 11a9 8 0 0 1-9 8c-2 0-3-.3-4-1l-6 3 2-6c-1-1-1-2-1-4a9 8 0 0 1 18 0ZM7 11h.01M12 11h.01M17 11h.01',
  windows: 'M3 5l8-1v8H3V5Zm10-1 8-1v9h-8V4ZM3 14h8v8l-8-1v-7Zm10 0h8v9l-8-1v-8Z',
  laptop: 'M4 3h16v13H4V3ZM2 20h20l-2-4H4l-2 4Z',
  lock: 'M5 10h14v11H5V10Zm3 0V7a4 4 0 0 1 8 0v3M12 14v3',
  clock: 'M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM12 7v5l3 2',
  settings: 'M4 6h16M4 12h16M4 18h16M8 3v6m8 0v6m-7 0v6',
  copy: 'M9 9h12v12H9V9ZM5 15H3V3h12v2',
  mail: 'M3 5h18v14H3V5Zm0 0 9 8 9-8',
  phone: 'M5 3h4l2 5-3 2a14 14 0 0 0 6 6l2-3 5 2v4a2 2 0 0 1-2 2C9 21 3 15 3 5a2 2 0 0 1 2-2Z',
  menu: 'M4 6h16M4 12h16M4 18h16',
  eye: 'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Zm10-3a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z',
  folder: 'M3 5h6l2 3h10v12H3V5Z',
  help: 'M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM9 9a3 3 0 1 1 4 3c-1 .5-1 1-1 2m0 3h.01',
}
</script>
<template><svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.65" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path :d="paths[name] || paths.grid" /></svg></template>
