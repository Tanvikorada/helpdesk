(() => {
  const cards = document.querySelectorAll('.tilt-card');

  cards.forEach((card) => {
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      const rotateY = ((x / rect.width) - 0.5) * 7;
      const rotateX = (0.5 - (y / rect.height)) * 6;
      card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-3px)`;
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = '';
    });
  });

  const elements = document.querySelectorAll('.hero, .tile, .submit-panel, .board, .auth-shell, .side-panel');
  elements.forEach((el, i) => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(16px)';
    setTimeout(() => {
      el.style.transition = 'opacity .45s ease, transform .45s ease';
      el.style.opacity = '1';
      el.style.transform = 'translateY(0)';
    }, 70 * i);
  });
})();
