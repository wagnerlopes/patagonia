// Eventos adicionados ao carregar a pagina
window.onload = function() {

  document.getElementById('btCalc').addEventListener('click', (e) => {
    calcularDistancia();
  });

}

// Função chamada ao clicar no botão
async function calcularDistancia() {
    const origemId = document.getElementById("origemId").value;
    const destinoId = document.getElementById("destinoId").value;
    const data = await getDistancia(origemId, destinoId);
    if (data) {
        document.getElementById('resultId').innerHTML = `<ul><li>Origem: ${data.origem}<li>Destino: ${data.destino}<li>Distância: ${data.distancia} ${data.unidade}`
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
