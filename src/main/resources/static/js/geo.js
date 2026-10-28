/******************************
 * Geo Map - Inicializa o Mapa
 * - Configura os botoes
 * - Chama info na API remota 
 ******************************/

let map;
let layerLocais;

window.addEventListener('load', () => map = map_init());

// Associa eventos após carregar a janela
window.onload = function() {
	
  document.getElementById('btSearch1').addEventListener('click', (e) => {
	const id = document.getElementById("origemId").value;
    console.log("Origem (ID) = " + id);
	carregarLocal(id, map);
  });

  document.getElementById('btSearch2').addEventListener('click', (e) => {
  const id = document.getElementById("destinoId").value;
    console.log("Destino (ID) = " + id);
  carregarLocal(id, map);
  });

  document.getElementById('btCalc').addEventListener('click', (e) => {
    calcularDistancia(map);
  });

  document.getElementById('btLimpar').addEventListener('click', (e) => {
    layerLocais.clearLayers();
	document.getElementById("lat1").value = "";
	document.getElementById("lon1").value = "";
	document.getElementById("lat2").value = "";
	document.getElementById("lon2").value = "";
  });

  document.getElementById('btCalcDist').addEventListener('click', (e) => {
    calcularDistanciaLocal(map);
  });
  	
  // adiciona evento de clique no mapa
  map.on('click', function(e) {
    const lat = e.latlng.lat;
    const lng = e.latlng.lng;

    // mostra no console
    console.log("Coordenadas clicadas:", lat, lng);

	// Salva coordenadas anteriores
	document.getElementById("lat2").value = document.getElementById("lat1").value;
	document.getElementById("lon2").value = document.getElementById("lon1").value;

	// Coordenadas atuais do ponto clicado  	
	document.getElementById("lat1").value = lat;
	document.getElementById("lon1").value = lng;
	
    // Cria marcador no ponto clicado
    L.marker([lat, lng])
      .bindPopup(`Coordenadas: ${lat.toFixed(5)}, ${lng.toFixed(5)}`)
      .addTo(layerLocais)
      .openPopup();
  });

}

// Inicializa o Mapa
const map_init = () => {
	
  // Lista de locais padrão
  const locais_array = [
	{
	  nome: "Brasília",
	  coords: [-15.79408736, -47.88790548],
	  descricao: "Capital Federal",
	  observacao: "Alt 1.100 m"
	},
    {
      nome: "Itupeva",
      coords: [-23.166864,  -47.061320],
      descricao: "Cidade da Rota das Frutas",
      observacao: "Alt 735 m"
    },
	{
	  nome: "Estação Geodésica 1730",
	  coords: [-23.2446210136, -46.9411063039],
	  descricao: "Jundiaí - Serra do Japi",
	  observacao: "SIRGAS2000 - Altitude 1.216 m"
	},
	{
	  nome: "Estação Geodésica 2862Z",
	  coords: [-23.1888888889, -46.8847222222],
	  descricao: "Jundiaí - Catedral Nossa Senhora do Desterro",
	  observacao: "SIRGAS2000 - Altitude 752 m"
    },
    {
      nome: "Estação Geodésica 93807",
      coords: [-23.1367396089, -47.1668108481],
      descricao: "Indaiatuba - Videiras",
      observacao: "SIRGAS2000 - Alt 637 m"
    },
    {
      nome: "Estação Geodésica 93945",
      coords: [-23.1796478483, -47.3030324261],
      descricao: "Salto - Jd São Gabriel",
      observacao: "SIRGAS2000 - Altitude 558 m"
    }
  ];

  // Centraliza mapa no ponto default
  const map = L.map('map').setView(locais_array[1].coords, 10);

  // Cria grupo de camadas de pontos relevantes
  layerEstacoes = L.layerGroup().addTo(map);
  layerLocais = L.layerGroup().addTo(map);
  
  // Cria as camadas de tiles do mapa
  const osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19
  });

  const esriSat = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles © Esri, USGS, NOAA',
    maxZoom: 19
  });

  const cartoLight = L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    attribution: '© OSM & © CARTO',
    subdomains: 'abcd',
    maxZoom: 20
  });

  // Controle lateral de camadas do mapa
  const baseMaps = {
    "OpenStreetMap": osm,
    "Esri Satélite": esriSat,
	"Carto Light": cartoLight
  };
  
  // Adiciona camada padrão (OSM)
  osm.addTo(map);

  // Marcadores padrão e rota entre eles
  const marcadorDF = L.marker(locais_array[0].coords).bindPopup("<b>" + locais_array[0].nome + "</b><br>" + locais_array[0].descricao + "</b><br>" + locais_array[0].observacao);
  const marcadorSP = L.marker(locais_array[1].coords).bindPopup("<b>" + locais_array[1].nome + "</b><br>" + locais_array[1].descricao + "</b><br>" + locais_array[1].observacao);
  var distanciaMetros = map.distance(locais_array[0].coords, locais_array[1].coords);
  var distanciaKm = (distanciaMetros / 1000).toFixed(1);

  const rota = L.polyline([marcadorDF.getLatLng(), marcadorSP.getLatLng()], {color: 'red'}).bindPopup("Rota Brasília → Itupeva " + distanciaKm + " Km");
  
  // Adiciona marcadores padrão ao mapa
  marcadorDF.addTo(map);
  marcadorSP.addTo(map);
  rota.addTo(map);

  // Adiciona estações geodésicas e suas informações
  locais_array.slice(2).forEach(function(local) {
    L.marker(local.coords)
     .addTo(layerEstacoes)
     .bindPopup("<b>" + local.nome + "</b><br>" + local.descricao + "</b><br>" + local.observacao);
  });
  
  // Controle lateral de pontos
  const overlayMaps = {
	"Brasília": marcadorDF,
    "Itupeva": marcadorSP,
    "Rota Brasília-Itupeva": rota,
	"Estações Geodésicas": layerEstacoes
  };

  L.control.layers(baseMaps, overlayMaps).addTo(map);

  // Adiciona escala gráfica ao mapa
  L.control.scale({
    position: 'bottomleft', // opções: 'topleft', 'topright', 'bottomleft', 'bottomright'
    imperial: false,        // desativa milhas/pés
    metric: true            // ativa metros/km
  }).addTo(map);

  return map;
};

// Função chamada ao clicar no botão search
async function carregarLocal(id, map) {
  const local = await buscarLocal(id);
  console.log(local);
  if (local) {
    // Centraliza no ponto
    map.setView(local.coords, 10);

    // Adiciona marcador
    L.marker(local.coords)
      .addTo(layerLocais)
      .bindPopup(`<b>Localidade: ${local.nome}</b><br>Município: ${local.municipio} - ${local.uf}<br>Distrito: ${local.distrito}<br>Microrregião: ${local.microrregiao}<br>Mesorregião: ${local.mesorregiao}`);
  }
}

// Função chamada ao clicar no botão distancia
async function calcularDistancia(map) {
    const origemId = document.getElementById("origemId").value;
    const destinoId = document.getElementById("destinoId").value;
	const origem = await buscarLocal(origemId);
	const destino = await buscarLocal(destinoId);
    const data = await getDistancia(origemId, destinoId);
    if (data) {
		console.log("Distância = ", data);
        const info = `<ul><li>Origem: ${data.origem}<li>Destino: ${data.destino}<li>Distância: ${data.distancia.toLocaleString('pt-BR')} ${data.unidade}`
		const rota = L.polyline([origem.coords, destino.coords], {color: 'red'}).bindPopup(info);
		rota.addTo(layerLocais);
		map.fitBounds(rota.getBounds());
		rota.openPopup();
  }
}

// Função chamada ao clicar btCalcDist (entre pontos clicados)
async function calcularDistanciaLocal(map) {
	const lat1 = document.getElementById("lat1").value;
	const lon1 = document.getElementById("lon1").value;
    const lat2 = document.getElementById("lat2").value;
	const lon2 = document.getElementById("lon2").value;
    const data = await getDistanciaLocal(lat1, lon1, lat2, lon2);
    if (data) {
		console.log("Distância = ", data);
 		const rota = L.polyline([[lat1, lon1], [lat2, lon2]], {color: 'red'}).bindPopup(`${(data/1000).toFixed(1)} Km`);
		rota.addTo(layerLocais);
		map.fitBounds(rota.getBounds());
		rota.openPopup();
  }
}

/* ------------------------- */
/* Chamadas da API Babilonia */
/* ------------------------- */

// Chamada assincrona da api remota localidade por ID
async function buscarLocal(id) {
  try {
    const response = await fetch(`https://localhost/babilonia/api/v1/localidade/id?id=${id}`, {
      method: "GET",
      headers: {
        "x-api-key": "3f6988fd-feff-477a-9772-c2ceefda3b39"
      }   
    });
    if (!response.ok) {
      throw new Error("Erro na requisição: " + response.status);
    }
    
    const data = await response.json();

    const local = { 
      nome: data.properties.localidade,
      coords: [data.geometry.coordinates[0], data.geometry.coordinates[1]], // Leaflet espera [lat, lon]
      municipio: data.properties.municipio,
      distrito: data.properties.distrito,
      microrregiao: data.properties.microrregiao,
      mesorregiao: data.properties.mesorregiao,
      uf: data.properties.uf
    };

    return local;
  } catch (error) {
    console.error("Erro ao buscar localidade: ", error);
    return null;
  }
}

// Chamada da api remota distancia entre localidades por ID
async function getDistancia(origemId, destinoId) {
  try {
	
    const response = await fetch(`https://localhost/babilonia/api/v1/localidade/distancia?origem=${origemId}&destino=${destinoId}`, {
      method: "GET",
      headers: {
        "x-api-key": "3f6988fd-feff-477a-9772-c2ceefda3b39"
      }   
    });
	
    if (!response.ok) {
      throw new Error("Erro na requisição: " + response.status);
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro calculando distância: ", error);
    return null;
  }
}

// Chamada da API Local distancia entre dois pontos
async function getDistanciaLocal(lat1, lon1, lat2, lon2) {
  try {
	
    const response = await fetch(`https://localhost:8080/patagonia/distancia?lat1=${lat1}&lon1=${lon1}&alt1=1&lat2=${lat2}&lon2=${lon2}&alt2=1`, {
      method: "GET"
    });
	
    if (!response.ok) {
      throw new Error("Erro na requisição: " + response.status);
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro calculando distância: ", error);
    return null;
  }
}
