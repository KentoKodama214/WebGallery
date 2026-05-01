function clickMenu(obj) {
	obj.classList.toggle('active');

	const element = document.getElementById('overlay');
	element.classList.toggle('open');
};