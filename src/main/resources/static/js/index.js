// Eventos adicionados ao carregar a pagina
document.addEventListener('DOMContentLoaded', () => {

  'use strict'

  // Obter ID da URL
  const id = new URLSearchParams(window.location.search).get("id");
  const munId = id && /^\d+$/.test(id) ? id : "3524006";
  
  // Placeholder visual
  const ctx = document.getElementById('myChart');

  // Labels and Colors default
  const xValues = ["2001","2002","2003","2004","2005","2006","2007","2008","2009","2010"];

  const colorValues = ["blueviolet","indigo","blue","limegreen","green","yellowgreen","yellow","gold","orange","coral"];

  // inicializa com zeros (evita layout shift)
  const initialData = [0,0,0,0,0,0,0,0,0,0];

  const chart = new Chart(ctx, {
    type: 'bar',
    data: { labels: xValues, datasets: [{ data: initialData, backgroundColor: colorValues }]},
    options: {
      plugins: {
        legend: { display: false },
        tooltip: { boxPadding: 3 }
      }
    }
  });

  getPopulacaoAll(munId, chart);
  
});

// Chamada API SIGRA IBGE - Série População do Município
async function getPopulacaoAll(munId, chart) {
  try {
    const response = await fetch(`https://apisidra.ibge.gov.br/values/t/6579/n6/${munId}/v/9324/p/all`, {method: "GET"});
    if (!response.ok) {
      throw new Error("API SIDRA IBGE requisição status: " + response.status);
    }
    const dados = await response.json();
    console.log(dados);
	
	const populacaoPorAno = dados.slice(1).map(item => ({
	    ano: item.D3N,
	    populacao: Number(item.V)
	}));
	console.log(populacaoPorAno);

	chart.data.labels = dados.slice(1).map(item => item.D3N);
	chart.data.datasets[0].data = dados.slice(1).map(item => Number(item.V));	
	chart.update();
  } catch (error) {
    console.error("Erro ao obter série população:", error);
    return null;
  }
}
