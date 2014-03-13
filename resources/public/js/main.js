function shit() {
  GetCellValues("level");
}

function showImage(where, src, width, height) {
    var img = document.createElement("img");
    img.src = src;
    img.width = width;
    img.height = height;

    where.appendChild(img);
}

var player_pos = null;
var lvl = Array();

function GetCellValues(from) {
  var table = document.getElementById(from);
  for (var r = 0, n = table.rows.length; r < n; r++) {
    lvl.push([]);
    for (var c = 0, m = table.rows[r].cells.length; c < m; c++) {
      var cell = table.rows[r].cells[c];
      lvl[lvl.length - 1].push(cell.firstChild.innerHTML);
      switch (cell.firstChild.innerHTML) {
        case 'w': addImg("wall.jpg", cell.firstChild.id, "42px"); break;
        case 'e': addImg("ground.jpg", cell.firstChild.id, "42px"); break;
        case 'g': addImg("ground.jpg", cell.firstChild.id, "42px");
                  cell.firstChild.style.width = "40px";
                  cell.firstChild.style.height = "40px";
                  cell.firstChild.style.border = "1px solid red";
                  break;
        case 'c': addImg("crate.png", cell.firstChild.id, "42px"); break;
        case 'p': addImg("player2.png", cell.firstChild.id, "42px");
                  player_pos = [c, r];
                  break;
        case 'gp': addImg("player2.png", cell.firstChild.id, "42px");
                   player_pos = [c, r];
                   break;
        case 'gc': addImg("crate.png", cell.firstChild.id, "42px");
                   cell.firstChild.style.width = "40px";
                   cell.firstChild.style.height = "40px";
                   cell.firstChild.style.border = "1px solid gold";
                   break;
      }
      cell.firstChild.firstChild.data = " ";
    }
  }
}

function addImg(img, srcId, size) {
  var src = document.getElementById(srcId);
  src.style.backgroundImage = "url(images/" + img + ")";
  src.style.backgroundSize = size;
}

document.onkeypress = khandle;

function khandle(e) {
  var char = String.fromCharCode(e.keyCode || e.charCode);
  switch(char) {
    case '&': move([0, -1]); //top
      break;
    case '%': move([-1, 0]); //left
      break;
    case '(': move([0, 1]); //down
      break;
    case '\'': move([1, 0]); //right
      break;
    case 'r': alert("restart")
      break;
  }
}

function refresh() {
  for (var r = 0; r < lvl.length; r++) {
    for (var c = 0; c < lvl[r].length; c++) {
      var cellDiv = document.getElementById(r + "_" + c);
      switch (lvl[r][c]) {
        case 'w': addImg("wall.jpg", cellDiv.id, "42px"); break;
        case 'e': addImg("ground.jpg", cellDiv.id, "42px"); break;
        case 'g': addImg("ground.jpg", cellDiv.id, "42px");
                  cellDiv.style.width = "40px";
                  cellDiv.style.height = "40px";
                  cellDiv.style.border = "1px solid red";
                  break;
        case 'c': addImg("crate.png", cellDiv.id, "42px"); break;
        case 'p': addImg("player2.png", cellDiv.id, "42px");
                  player_pos = [c, r];
                  break;
        case 'gp': addImg("player2.png", cellDiv.id, "42px");
                   player_pos = [c, r];
                   break;
        case 'gc': addImg("crate.png", cellDiv.id, "42px");
                   cellDiv.style.width = "40px";
                   cellDiv.style.height = "40px";
                   cellDiv.style.border = "1px solid gold";
                   break;
      }
    }
  }
}

function move(direction) {
  var destination = [direction[0] + player_pos[0], direction[1] + player_pos[1]];
  var cellDiv = document.getElementById(destination.join("_"));
  var value = lvl[destination[1]][destination[0]];
  switch(value) {
    case 'e':
      if (lvl[player_pos[1]][player_pos[0]] == "gp") {
        lvl[player_pos[1]][player_pos[0]] = "g";
      } else {
        lvl[player_pos[1]][player_pos[0]] = 'e';
      }
      lvl[destination[1]][destination[0]] = "p";
      break;
    case 'c':
      if (lvl[destination[1] + direction[1]][destination[0] + direction[0]] == "g") {
        lvl[player_pos[1]][player_pos[0]] = 'e';
        if (lvl[destination[1]][destination[0]] == "gc") {
          lvl[destination[1]][destination[0]] = "gp";
        } else {
          lvl[destination[1]][destination[0]] = "p";
        }
        lvl[destination[1] + direction[1]][destination[0] + direction[0]] = "gc";
      } else if (lvl[destination[1] + direction[1]][destination[0] + direction[0]] == "e") {
        lvl[player_pos[1]][player_pos[0]] = 'e';
        lvl[destination[1]][destination[0]] = "p";
        lvl[destination[1] + direction[1]][destination[0] + direction[0]] = "c";
      }
      break;
    case 'w':
      break;
    case 'gc':
      lvl[player_pos[1]][player_pos[0]] = 'e';
      lvl[destination[1]][destination[0]] = "gp";
      lvl[destination[1] + direction[1]][destination[0] + direction[0]] = "c";
      break;
    case 'g':
      lvl[player_pos[1]][player_pos[0]] = 'e';
      lvl[destination[1]][destination[0]] = "gp";
      break;
  }
  refresh();
}
