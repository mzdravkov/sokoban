function draw() {
  var canvas = document.getElementById("field");
  if (!(canvas.getContext == null)) {
    var context = canvas.getContext("2d");
    context.fillStyle = "rgb(150, 29, 28)";
    context.fillRect(10, 10, 100, 100);
    return null;
  } else {
    return alert("This pages uses HTML5 to render correctly. Be modern and stop using shitty browsers. Please.");
  }
};

function shit() {
  GetCellValues("level");
}

function GetCellValues(from) {
  var table = document.getElementById(from);
  for (var r = 0, n = table.rows.length; r < n; r++) {
    for (var c = 0, m = table.rows[r].cells.length; c < m; c++) {
      //alert(table.rows[r].cells[c].innerHTML);
      var cell = table.rows[r].cells[c]
      if (cell.firstChild.firstChild.data == "e") {
        cell.firstChild.firstChild.data = " ";
      }
      cell.firstChild.style.width = "20px";
      cell.firstChild.style.height = "20px";
      switch (cell.firstChild.innerHTML) {
        case 'w': cell.firstChild.style.backgroundColor = "black"; break;
        case 'e': cell.firstChild.style.backgroundColor = "white"; break;
        case 'g': cell.firstChild.style.backgroundColor = "purple"; break;
        case 'c': cell.firstChild.style.backgroundColor = "yellow"; break;
        case 'o': cell.firstChild.style.backgroundColor = "white";
                  cell.firstChild.firstChild.data = 'o';
                  break;
        case 'gc': cell.firstChild.style.backgroundColor = "gold"; break;
      }
    }
  }
}
