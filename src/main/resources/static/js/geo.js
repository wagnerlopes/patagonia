/******************************
 * Geo Map - Inicializa o Mapa
 * - Configura os botoes
 * - Chama info na API remota 
 ******************************/

let map;

let layerLocais;

window.addEventListener('load', () => map = map_init());


window.onload = function() {
	
  document.getElementById('btSearch').addEventListener('click', (e) => {
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
  });
	
  // adiciona evento de clique no mapa
  map.on('click', function(e) {
    const lat = e.latlng.lat;
    const lng = e.latlng.lng;

    // mostra no console
    console.log("Coordenadas clicadas:", lat, lng);

    // opcional: criar marcador no ponto clicado
    L.marker([lat, lng])
      .bindPopup(`Coordenadas: ${lat.toFixed(5)}, ${lng.toFixed(5)}`)
      .addTo(layerLocais)
      .openPopup();
  });

}

// Inicializa o Mapa
const map_init = () => {
	
  // Lista de locais de exemplo
  const locais_array = [
	{
	  nome: "Brasília",
	  coords: [-15.79408736, -47.88790548],
	  descricao: "Reconhecida por planejamento urbano e áreas verdes.",
	  populacao: "3.123.345 hab"
	},
    {
      nome: "Itupeva",
      coords: [-23.15340963, -47.05570115],
      descricao: "Cidade da Rota das Frutas.",
      populacao: "73.345 hab"
    },
	{
	  nome: "Estação Geodésica 93792",
	  coords: [-23.2361132578, -46.8739280342],
	  descricao: "ETEVAV",
	  populacao: "SIRGAS2000"
	}
	
  ];

  // Inicializa o mapa
  const map = L.map('map').setView(locais_array[0].coords, 5);

  // Camadas de tiles
  const osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19
  });

  const cartoLight = L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    attribution: '© OSM & © CARTO',
    subdomains: 'abcd',
    maxZoom: 20
  });

  const esriSat = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles © Esri, USGS, NOAA',
    maxZoom: 19
  });

  // Controle de camadas
  const baseMaps = {
    "OpenStreetMap": osm,
    "Carto Light": cartoLight,
    "Esri Satélite": esriSat
  };
  
  // Adiciona camada padrão (OSM)
  osm.addTo(map);

  // Camadas extras
  const marcadorDF = L.marker(locais_array[0].coords).bindPopup(locais_array[0].nome);
  const marcadorSP = L.marker(locais_array[1].coords).bindPopup(locais_array[1].nome);
  const rota = L.polyline([marcadorDF.getLatLng(), marcadorSP.getLatLng()], {color: 'red'}).bindPopup("Rota Brasília → Itupeva");

  // Agrupando extras
  const overlayMaps = {
	"Marcador DF": marcadorDF,
    "Marcador SP": marcadorSP,
    "Rota DF-SP": rota
  };

  L.control.layers(baseMaps, overlayMaps).addTo(map);

  // Adiciona extras por padrão
  marcadorDF.addTo(map);
  marcadorSP.addTo(map);
  rota.addTo(map);

  // Adiciona marcadores com informações extras
  locais_array.forEach(function(local) {
    L.marker(local.coords)
      .addTo(map)
      .bindPopup("<b>" + local.nome + "</b><br>" + local.descricao + "</b><br>" + local.populacao);
  });

  // Adiciona escala
  L.control.scale({
    position: 'bottomleft', // opções: 'topleft', 'topright', 'bottomleft', 'bottomright'
    imperial: false,        // desativa milhas/pés
    metric: true            // ativa metros/km
  }).addTo(map);

  // cria um grupo de camadas
  layerLocais = L.layerGroup().addTo(map);
    
  return map;

};

// Chamada assincrona da api remota localidade
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
    console.error("Erro ao buscar dados:", error);
    return null;
  }
}

// Função chamada ao clicar no botão search
async function carregarLocal(id, map) {
  const local = await buscarLocal(id);
  console.log(local);
  if (local) {
    // Centraliza no ponto
    map.setView(local.coords, 6);

    // Adiciona marcador
    L.marker(local.coords)
      .addTo(layerLocais)
      .bindPopup(`<b>Localidade: ${local.nome}</b><br>Município: ${local.municipio} - ${local.uf}<br>Distrito: ${local.distrito}<br>Microrregião: ${local.microrregiao}<br>Mesorregião: ${local.mesorregiao}`);
  }
}

// Chamada da api remota
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
    console.log(data);
    return data;
  } catch (error) {
    console.error("Erro calculando distância:", error);
    return null;
  }
}

// Função assincrona chamada ao clicar no botão distancia
async function calcularDistancia(map) {
    const origemId = document.getElementById("origemId").value;
    const destinoId = document.getElementById("destinoId").value;
	const origem = await buscarLocal(origemId);
	const destino = await buscarLocal(destinoId);
    const data = await getDistancia(origemId, destinoId);
    if (data) {
        const info = `<ul><li>Origem: ${data.origem}<li>Destino: ${data.destino}<li>Distância: ${data.distancia.toLocaleString('pt-BR')} ${data.unidade}`
		const rota = L.polyline([origem.coords, destino.coords], {color: 'red'}).bindPopup(info);
		rota.addTo(layerLocais);
		map.fitBounds(rota.getBounds());
		rota.openPopup();
  }
}
