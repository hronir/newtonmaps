var isZoomed = false;
function zoom() {
	isZoomed = !isZoomed;
	var obj;
	obj = document.getElementById('app');
	if (isZoomed) {
		obj.className="app-zoom";
	} else {
		obj.className="app-normal";
	}
}