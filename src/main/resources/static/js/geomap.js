/******************************
 * Geo Map - Inicializa o Mapa
 * - Configura os botoes
 * - Chama info na API remota 
 ******************************/

const App = {
  map: null,
  layerLocais: null,
  layerEstacoes: null
};

document.addEventListener('DOMContentLoaded', init);

function init() {
  initMap();

  if (!(App.map instanceof L.Map)) {
      throw new Error('Falha ao inicializar o mapa');
  }
    
  bindEvents();
}

// Inicializar eventos
function bindEvents() {

  const munOri = document.getElementById('munOri');
  if (munOri) {
    munOri.addEventListener('change', (e) => {
	  const id = e.target.value;
	  carregarLocalidades(id, 'origemId');
    });
  }
  
  const origemId = document.getElementById('origemId');
  if (origemId) {
    origemId.addEventListener('change', (e) => {
	  const id = e.target.value;
      console.log("Origem (ID) = " + id);
	  carregarLocal(id);
    });
  }
  
  const munDes = document.getElementById('munDes');
  if (munDes) {
    munDes.addEventListener('change', (e) => {
      const id = e.target.value;
      carregarLocalidades(id, 'destinoId');
    });
  }
  
  const destinoId = document.getElementById('destinoId');
  if (destinoId) {
    destinoId.addEventListener('change', (e) => {
      const id = e.target.value;
      console.log("Destino (ID) = " + id);
      carregarLocal(id);
    });
  }
  
  const btCalc = document.getElementById('btCalc');
  if (btCalc) {
    btCalc.addEventListener('click', (e) => {
      calcularDistancia();
    });
  }
  
  const btLimpar = document.getElementById('btLimpar');
  if (btLimpar) {
    btLimpar.addEventListener('click', (e) => {
	  if (App.layerLocais) {
        App.layerLocais.clearLayers();
	  }
	  document.getElementById("lat1").value = "";
	  document.getElementById("lon1").value = "";
	  document.getElementById("lat2").value = "";
	  document.getElementById("lon2").value = "";
    });
  }

  const btCalcDist = document.getElementById('btCalcDist');
  if (btCalcDist) {
    document.getElementById('btCalcDist').addEventListener('click', (e) => {
      calcularDistanciaLocal();
    });
  }
	
  // adiciona evento de clique no mapa
  App.map.on('click', function(e) {
    const lat = e.latlng.lat;
    const lng = e.latlng.lng;

    // mostra no console
    console.log("Coordenadas clicadas:", lat, lng);

	// Salva coordenadas anteriores
	document.getElementById("lat1").value = document.getElementById("lat2").value;
	document.getElementById("lon1").value = document.getElementById("lon2").value;

	// Coordenadas atuais do ponto clicado  	
	document.getElementById("lat2").value = lat;
	document.getElementById("lon2").value = lng;
	
    // Cria marcador no ponto clicado
    L.marker([lat, lng])
      .bindPopup(`Coordenadas: ${lat.toFixed(5)}, ${lng.toFixed(5)}`)
      .addTo(App.layerLocais)
      .openPopup();
  });

  // Obtendo localização aproximada pelo navegador
  App.map.locate({setView: true, maxZoom: 16});

  App.map.on('locationfound', function(e) {
      L.marker(e.latlng)
       .addTo(App.map)
       .bindPopup('Local atual aproximado')
       .openPopup();
  });

  App.map.on('locationerror', function(e) {
      alert(e.message);
  });

}

// Inicializa o Mapa
function initMap() {
	
  // Lista de locais padrão
  const locais_array = [
	{
	  nome: "Brasília",
	  coords: [-15.79408736, -47.88790548],
	  descricao: "Capital Federal",
	  observacao: "Alt 1.100 m"
	},
	{
	  nome: "São Paulo",
	  coords: [ -23.550385,  -46.633956],
	  descricao: "Marco zero da cidade de São Paulo",
	  observacao: "Alt 760 m"
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
	  nome: "Estação Geodésica 2852Z",
	  coords: [-22.8511111111, -47.1166666667],
	  descricao: "Campinas - SP-340",
	  observacao: "SIRGAS2000 - Altitude 599 m"
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
      descricao: "Indaiatuba - Bairro Videiras",
      observacao: "SIRGAS2000 - Alt 637 m"
    },
    {
      nome: "Estação Geodésica 93945",
      coords: [-23.1796478483, -47.3030324261],
      descricao: "Salto - Jardim São Gabriel",
      observacao: "SIRGAS2000 - Altitude 558 m"
    }
  ];

  // Centraliza mapa no ponto default
  App.map = L.map('map').setView(locais_array[1].coords, 10);

  // Cria grupo de camadas de pontos relevantes
  App.layerEstacoes = L.layerGroup().addTo(App.map);
  App.layerLocais = L.layerGroup().addTo(App.map);
  
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
  osm.addTo(App.map);

  // Marcadores padrão e rota entre eles
  const marcadorDF = L.marker(locais_array[0].coords).bindPopup("<b>" + locais_array[0].nome + "</b><br>" + locais_array[0].descricao + "</b><br>" + locais_array[0].observacao);
  const marcadorSP = L.marker(locais_array[1].coords).bindPopup("<b>" + locais_array[1].nome + "</b><br>" + locais_array[1].descricao + "</b><br>" + locais_array[1].observacao);
  const marcadorIT = L.marker(locais_array[2].coords).bindPopup("<b>" + locais_array[2].nome + "</b><br>" + locais_array[2].descricao + "</b><br>" + locais_array[2].observacao);
  var distanciaMetros = App.map.distance(locais_array[0].coords, locais_array[1].coords);
  var distanciaKm = (distanciaMetros / 1000).toFixed(1);

  const rota = L.polyline([marcadorDF.getLatLng(), marcadorSP.getLatLng()], {color: 'red'}).bindPopup(`Rota Brasília → São Paulo<br>Distância: ${distanciaKm} Km`);
  
  // Adiciona marcadores padrão ao mapa
  marcadorIT.addTo(App.map);
  marcadorDF.addTo(App.map);
  marcadorSP.addTo(App.map);
  rota.addTo(App.map);

  // Adiciona estações geodésicas e suas informações
  locais_array.slice(3).forEach(function(local) {
    L.marker(local.coords)
     .addTo(App.layerEstacoes)
     .bindPopup("<b>" + local.nome + "</b><br>" + local.descricao + "</b><br>" + local.observacao);
  });

  // Raio de 1km do local padrão
  const circulo = L.circle(marcadorIT.getLatLng(), {
      radius: 1000,
      color: 'red',
      fillColor: '#f03',
      fillOpacity: 0.5
  }).addTo(App.map);
    
  // Controle lateral de pontos
  const overlayMaps = {
	"Brasília": marcadorDF,
	"São Paulo": marcadorSP,
	"Itupeva": marcadorIT,
    "Rota Brasília-São Paulo": rota,
	"Estações Geodésicas": App.layerEstacoes,
    "Raio de 1 Km": circulo
  };

  L.control.layers(baseMaps, overlayMaps).addTo(App.map);

  // Adiciona escala gráfica ao mapa
  L.control.scale({
    position: 'bottomleft', // opções: 'topleft', 'topright', 'bottomleft', 'bottomright'
    imperial: false,        // desativa milhas/pés
    metric: true            // ativa metros/km
  }).addTo(App.map);

}


// Função chamada ao selecionar municipio de origem ou destino
async function carregarLocalidades(ibgeId, htmlSelectId) {
	
  const localidades = await getLocalidades(ibgeId);

  const select = document.getElementById(htmlSelectId);

  // Limpa opções existentes
  select.innerHTML = '<option value="">Selecione a localidade...</option>';

  // Adiciona novas opções
  Object.entries(localidades)
    .sort(([, x], [, y]) => x.localeCompare(y, 'pt-BR'))
    .forEach(([id, descricao]) => {
      const option = document.createElement('option');
      option.value = id;
      option.textContent = descricao;
      select.appendChild(option);
    });
}

// Função chamada ao clicar no botão search
async function carregarLocal(id) {
  const local = await buscarLocal(id);
  console.log(local);
  if (local) {
    // Centraliza no ponto
    App.map.flyTo(local.coords, 10);

    // Adiciona marcador
    L.marker(local.coords)
      .addTo(App.layerLocais)
      .bindPopup(`<b>Localidade: ${local.nome}</b><br>Município: ${local.municipio} - ${local.uf} (${local.munIbgeId})<br>Distrito: ${local.distrito}<br>Microrregião: ${local.microrregiao}<br>Mesorregião: ${local.mesorregiao}`);
  }
}

// Função chamada ao clicar no botão distancia
async function calcularDistancia() {
    const origemId = document.getElementById("origemId").value;
    const destinoId = document.getElementById("destinoId").value;
	const origem = await buscarLocal(origemId);
	const destino = await buscarLocal(destinoId);
    const data = await getDistancia(origemId, destinoId);
    if (data) {
		console.log("Distância = ", data);
        const info = `<ul><li>Origem: ${data.origem}<li>Destino: ${data.destino}<li>Distância: ${data.distancia.toLocaleString('pt-BR')} ${data.unidade}`
		const rota = L.polyline([origem.coords, destino.coords], {color: 'red'}).bindPopup(info);
		rota.addTo(App.layerLocais);
		App.map.fitBounds(rota.getBounds());
		rota.openPopup();
  }
}

// Função chamada ao clicar btCalcDist (entre pontos clicados)
async function calcularDistanciaLocal() {
	const lat1 = document.getElementById("lat1").value;
	const lon1 = document.getElementById("lon1").value;
    const lat2 = document.getElementById("lat2").value;
	const lon2 = document.getElementById("lon2").value;
    const data = await getDistanciaLocal(lat1, lon1, lat2, lon2);
    if (data) {
		console.log("Info: ", data);
		const azv = Number.isNaN(data.azv) ? "Valor inválido" : Number(data.azv).toFixed(1);
		const azm = Number.isNaN(data.azm) ? "Valor inválido" : Number(data.azm).toFixed(1);
 		const rota = L.polyline([[lat1, lon1], [lat2, lon2]], {color: 'red'}).bindPopup(`<ul><li>Distância: ${(data.distancia/1000).toFixed(1)} Km<li>AzV: ${azv}°<li>AzM:${azm}°</ul>`);
		rota.addTo(App.layerLocais);
		App.map.fitBounds(rota.getBounds());
		rota.openPopup();
  }
}

/* ------------------------- */
/* Chamadas da API Babilonia */
/* ------------------------- */

// Chamada assincrona da api remota localidade por ID
async function buscarLocal(id) {
  try {
    const response = await fetch(`https://babilonia.local/api/v1/localidade/id?id=${id}`, {
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
	  munIbgeId: data.properties.munIbgeID,
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
	
    const response = await fetch(`https://babilonia.local/api/v1/localidade/distancia?origem=${origemId}&destino=${destinoId}`, {
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

// Chamada da api remota localidades de municipio por IBGE_ID
async function getLocalidades(ibgeId) {
  try {
	
    const response = await fetch(`https://babilonia.local/api/v1/localidade/municipio?ibgeId=${ibgeId}`, {
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
    console.error("Erro consultando localidades: ", error);
    return null;
  }
}

// Chamada da API Local distancia entre dois pontos
async function getDistanciaLocal(lat1, lon1, lat2, lon2) {
  try {
	
    const response = await fetch(`https://patagonia.local:8443/distancia?lat1=${lat1}&lon1=${lon1}&alt1=1&lat2=${lat2}&lon2=${lon2}&alt2=1`, {
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

// Chamada da API Local Mun Info
async function getMunInfo(id) {
  try {
	
    const response = await fetch(`https://patagonia.local:8443/geomap/muninfo?id=${id}`, {
      method: "GET"
    });
	
    if (!response.ok) {
      throw new Error("Erro na requisição: " + response.status);
    }
    
    const dados = await response.json();
    return dados;
  } catch (error) {
    console.error("Erro Mun Info: ", error);
    return null;
  }
}
