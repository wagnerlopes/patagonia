document.addEventListener('DOMContentLoaded', init);

// Eventos adicionados ao carregar a pagina
function init(){
	
  // Configura Botão favoritos
  initFavoritos();

  // Configura Botão tema claro/escuro 	
  initButtonTheme();
  
  // Configura Botão de usuário 	
  initUserLog();

  // Inicializa gráficos quando tem id
  const id = new URLSearchParams(window.location.search).get("id");
  const municipioValido = id && /^\d+$/.test(id); // existe id e id é numérico
  if (municipioValido) {
    initCharts(id);
  }

}

// Inicializa botão favoritos
function initFavoritos() {
  document.getElementById('favoritos').addEventListener('click', () => {
	document.getElementById('municipio').value = 3524006;
	document.getElementById('form1').requestSubmit();
  });
}

// Inicializa botão de usuário
function initUserLog() {
  document.getElementById('userLog').addEventListener('click', () => {
	alert('Olá usuário!');
  });
}

// Inicializa botão de tema claro/escuro
function initButtonTheme() {
  const html = document.documentElement;
  localStorage.setItem('theme', html.getAttribute('data-bs-theme'));

  document.getElementById('themeToggle').addEventListener('click', () => {
    const current = html.getAttribute('data-bs-theme') || 'light';
    const next = current === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-bs-theme', next);
    localStorage.setItem('theme', next);
	document.getElementById('themeIcon').className = next === 'dark' ? 'bi bi-sun' : 'bi bi-moon';
  });
}
  
// Inicializa graficos quando tem id de município válido
function initCharts(munId) {
  // Placeholder visual
  const popChartHtml = document.getElementById('popChart');
  const pibChartHtml = document.getElementById('pibChart');

  // Labels and Colors default
  const xValues = ["2001","2002","2003","2004","2005","2006","2007","2008","2009","2010"];

  // inicializa com zeros (evita layout shift)
  const initialData = [0,0,0,0,0,0,0,0,0,0];

  const popChart = new Chart(popChartHtml, {
	  type: 'bar',
	  data: { labels: xValues,
		    datasets: [{
				 data: initialData,
				 backgroundColor: 'rgba(54, 162, 235, 0.7)',
			     borderColor: 'rgba(54, 162, 235, 1)',
			     borderWidth: 1
		 	}]
	},
	  options: {
	    plugins: {
	      legend: { display: false },
	      tooltip: { boxPadding: 3 }
	    }
	  }
  });

  const pibChart = new Chart(pibChartHtml, {
	  type: 'bar',
	  data: { labels: xValues,
	        datasets: [{
				 data: initialData,
				 backgroundColor: 'rgba(40, 167, 69, 0.7)',
			     borderColor: 'rgba(40, 167, 69, 1)',
			     borderWidth: 1
		 	}]
	},
	  options: {
	    plugins: {
	      legend: { display: false },
	      tooltip: {
	        boxPadding: 3,
		    callbacks: {
		    label: function(context) {
		             return 'R$ ' +
			                context.raw.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1}) +
			                ' milhões';
			       }
		    }
	      }
	    }
	  }
  });

  getPopulacaoAll(munId, popChart);
  getPibAll(munId, pibChart);
}


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

// Chamada API SIGRA IBGE - Série PIB do Município
async function getPibAll(munId, chart) {
  try {
    const response = await fetch(`https://apisidra.ibge.gov.br/values/t/5938/n6/${munId}/v/37/p/all`, {method: "GET"});
    if (!response.ok) {
      throw new Error("API SIDRA IBGE requisição status: " + response.status);
    }
    const dados = await response.json();
    console.log(dados);
	
	const pibPorAno = dados.slice(1).map(item => ({
	    ano: item.D3N,
	    pib: Number(item.V) / 1000
	}));
	console.log(pibPorAno);

	chart.data.labels = dados.slice(1).map(item => item.D3N);
	chart.data.datasets[0].data = dados.slice(1).map(item => Number(item.V) / 1000);
	chart.update();
  } catch (error) {
    console.error("Erro ao obter série pib:", error);
    return null;
  }
}
